#!/usr/bin/env bash
# ============================================================================
# Hook: 自动 PR 提交 (Auto Submit PR)
# 触发时机: Stop (会话结束)
# 功能: 检测未提交/未推送变更 → 自动 commit → push → 创建 PR
# 安全: 不在 master/main 分支上自动操作; 检测已有 PR 避免重复创建
# ============================================================================

set -e

# 配置
TARGET_BRANCH="${PR_TARGET_BRANCH:-master}"
PROJECT_DIR="${CLAUDE_PROJECT_DIR:-$(pwd)}"

cd "$PROJECT_DIR"

# ── 前置检查 ───────────────────────────────────────────────────────────────

# 检查 gh CLI 是否已认证
if ! gh auth status &>/dev/null; then
  echo "⚠️  gh CLI 未认证，跳过自动 PR。运行 'gh auth login' 后即可生效。"
  exit 0
fi

# 获取当前分支
CURRENT_BRANCH=$(git branch --show-current 2>/dev/null || true)

# 在目标分支上不自动操作 (安全第一)
if [ "$CURRENT_BRANCH" = "$TARGET_BRANCH" ] || [ "$CURRENT_BRANCH" = "main" ]; then
  echo "ℹ️  当前在 $CURRENT_BRANCH 分支，跳过自动 PR 提交。"
  exit 0
fi

# 没有远程仓库不操作
if ! git remote get-url origin &>/dev/null; then
  echo "ℹ️  未配置远程仓库 origin，跳过自动 PR。"
  exit 0
fi

# ── 检测变更 ────────────────────────────────────────────────────────────────

HAS_UNCOMMITTED=false
HAS_UNPUSHED=false

# 检查未提交的变更（包括已追踪文件的修改 + 未追踪的新文件）
if ! git diff-index --quiet HEAD -- 2>/dev/null; then
  HAS_UNCOMMITTED=true
fi

# 检查未追踪的新文件
if [ -n "$(git ls-files --others --exclude-standard 2>/dev/null)" ]; then
  HAS_UNCOMMITTED=true
fi

# 检查未推送的提交
if git rev-parse --abbrev-ref @{u} &>/dev/null; then
  if [ -n "$(git log @{u}..HEAD --oneline 2>/dev/null)" ]; then
    HAS_UNPUSHED=true
  fi
else
  # 分支还没有设置 upstream，任何本地提交都算未推送
  if [ -n "$(git log origin/$TARGET_BRANCH..HEAD --oneline 2>/dev/null)" ]; then
    HAS_UNPUSHED=true
  fi
fi

# 没有任何需要处理的内容
if ! $HAS_UNCOMMITTED && ! $HAS_UNPUSHED; then
  exit 0
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🤖 Auto-Submit PR: 检测到待提交的变更"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# ── 步骤 1: 提交未暂存变更 ──────────────────────────────────────────────────

if $HAS_UNCOMMITTED; then
  echo "📦 正在自动提交未暂存的变更..."

  git add -A

  # 根据变更内容自动生成 commit message
  CHANGED_FILES=$(git diff --cached --name-only 2>/dev/null | sort)
  FILE_COUNT=$(echo "$CHANGED_FILES" | wc -l)

  # 尝试推断变更类型
  COMMIT_TYPE="feat"
  if echo "$CHANGED_FILES" | grep -qE '(test|Test|\.test\.|\.spec\.)'; then
    COMMIT_TYPE="test"
  elif echo "$CHANGED_FILES" | grep -qE '(\.md$|README|CHANGELOG|docs/)'; then
    COMMIT_TYPE="docs"
  elif echo "$CHANGED_FILES" | grep -qE '(\.claude/)'; then
    COMMIT_TYPE="chore"
  elif git diff --cached --diff-filter=M --name-only 2>/dev/null | head -5 | xargs -I{} git diff --cached {} 2>/dev/null | grep -qE '^\+\s*(fix|修复|bug)'; then
    COMMIT_TYPE="fix"
  fi

  # 生成文件摘要作为 commit 描述
  FILE_SUMMARY=$(echo "$CHANGED_FILES" | head -3 | tr '\n' ' ' | sed 's/ $//; s/  */ /g')
  if [ "$FILE_COUNT" -gt 3 ]; then
    FILE_SUMMARY="$FILE_SUMMARY 等 ${FILE_COUNT} 个文件"
  fi

  COMMIT_MSG="$COMMIT_TYPE: $FILE_SUMMARY"

  git commit -m "$COMMIT_MSG" \
    -m "Co-Authored-By: Claude <noreply@anthropic.com>" 2>&1

  echo "✅ 已提交: $COMMIT_MSG"
  HAS_UNPUSHED=true
fi

# ── 步骤 2: 推送到远程仓库 ──────────────────────────────────────────────────

if $HAS_UNPUSHED; then
  echo "🚀 正在推送到 origin/$CURRENT_BRANCH..."

  if git push -u origin "$CURRENT_BRANCH" 2>&1; then
    echo "✅ 推送成功"
  else
    echo "❌ 推送失败，请检查网络或权限。不会重试。"
    exit 0
  fi
fi

# ── 步骤 3: 创建 Pull Request ────────────────────────────────────────────────

# 检查是否已有打开的 PR（避免重复创建）
EXISTING_PR=$(gh pr list \
  --head "$CURRENT_BRANCH" \
  --state open \
  --json number,url \
  --jq '.[0] | "\(.number)|\(.url)"' 2>/dev/null || true)

if [ -n "$EXISTING_PR" ]; then
  PR_NUM=$(echo "$EXISTING_PR" | cut -d'|' -f1)
  PR_URL=$(echo "$EXISTING_PR" | cut -d'|' -f2)
  echo "ℹ️  分支 $CURRENT_BRANCH 已有打开的 PR (#$PR_NUM)"
  echo "🔗 $PR_URL"
  exit 0
fi

echo "🔀 正在创建 Pull Request → $TARGET_BRANCH..."

# PR 标题: 使用最近的 commit 标题
PR_TITLE=$(git log -1 --pretty=%s 2>/dev/null || echo "feat: update $CURRENT_BRANCH")

# PR 正文: 包含变更摘要
PR_BODY=$(
  cat <<EOF
## 📋 变更概要

$(git log origin/$TARGET_BRANCH..HEAD --oneline --no-merges 2>/dev/null | sed 's/^/- `/; s/$/`/' || echo "自动提交")

## 📁 变更文件

$(git diff --stat origin/$TARGET_BRANCH..HEAD 2>/dev/null || git diff --stat HEAD~1 2>/dev/null || echo "无文件统计")

---
🤖 由 [Claude Code](https://claude.com/claude-code) 自动生成 | Co-Authored-By: Claude <noreply@anthropic.com>
EOF
)

# 执行 PR 创建
PR_URL=$(gh pr create \
  --base "$TARGET_BRANCH" \
  --head "$CURRENT_BRANCH" \
  --title "$PR_TITLE" \
  --body "$PR_BODY" 2>&1)

if [ $? -eq 0 ] && [ -n "$PR_URL" ]; then
  echo ""
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  echo "  🎉 Pull Request 已自动创建!"
  echo "  🔗 $PR_URL"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  echo ""
else
  echo "❌ PR 创建失败: $PR_URL"
fi
