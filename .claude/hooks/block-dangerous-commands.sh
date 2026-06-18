#!/bin/bash
# .claude/hooks/block-dangerous-commands.sh

INPUT=$(cat)
COMMAND=$(echo "$INPUT" | jq -r '.tool_input.command // empty')

# 定义危险模式（正则匹配）
DANGEROUS_PATTERNS=(
    'rm\s+-rf\s+/'            # rm -rf 根目录或绝对路径
    'git\s+push\s+.*--force'  # force push
    'DROP\s+TABLE'            # 删表
    'DROP\s+DATABASE'         # 删库
    'git\s+reset\s+--hard'    # 丢弃未提交修改
    '>\s*/dev/sd'             # 写入磁盘设备
)

for pattern in "${DANGEROUS_PATTERNS[@]}"; do
    if echo "$COMMAND" | grep -iEq "$pattern"; then
        # 退出码 2 = 阻塞性错误，Claude 会收到拒绝通知
        echo "BLOCKED: 命令匹配危险模式 [$pattern]" >&2
        echo "原始命令: $COMMAND" >&2
        exit 2
    fi
done

# 通过检查，允许执行
exit 0
