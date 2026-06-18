---
description: 手动提交当前分支变更并创建 Pull Request
---

将当前 feature 分支的变更自动提交、推送并创建 PR。

## 执行步骤

1. **检查当前分支**: 如果在 master/main 上，拒绝执行
2. **检查 gh 认证**: 未认证则提示 `gh auth login`
3. **检查变更**: `git status` 查看未提交/未推送内容
4. **如果无任何变更**: 提示 `没有需要提交的内容`
5. **执行提交**: 如果有未暂存变更，全部 stage 并生成 commit message
6. **推送**: `git push -u origin <branch>`
7. **创建 PR**: 使用 `gh pr create --base master --head <branch>`
   - 如果 PR 已存在，输出已有 PR 链接
   - 否则创建新 PR 并输出链接

## 安全规则

- 不在 master/main 分支上执行
- PR 已存在时不重复创建
- 推送失败时停止并报告错误
