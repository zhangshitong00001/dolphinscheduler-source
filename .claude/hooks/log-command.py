#!/usr/bin/env python3
# ============================================================================
# Claude Code 执行日志记录器 (Python 版)
# 从 PostToolUse Hook 的 stdin JSON 中读取工具调用详情，
# 记录到按日期命名的日志文件。
# ============================================================================

import json
import sys
import os
from datetime import datetime

LOG_DIR = os.path.join(os.environ.get("CLAUDE_PROJECT_DIR", "."), ".claude", "logs")
os.makedirs(LOG_DIR, exist_ok=True)

LOG_FILE = os.path.join(LOG_DIR, f"commands-{datetime.now():%Y%m%d}.log")

def truncate(s: str, max_len: int = 300) -> str:
    """截断过长的字符串"""
    if len(s) <= max_len:
        return s
    return s[:max_len] + f" ... (共{len(s)}字符)"

def fmt_time() -> str:
    return datetime.now().strftime("%Y-%m-%d %H:%M:%S")

def main():
    raw = sys.stdin.read()
    if not raw.strip():
        return

    try:
        data = json.loads(raw)
    except json.JSONDecodeError:
        return

    tool_name = data.get("tool_name", "unknown")
    tool_input = data.get("tool_input", {})
    tool_response = data.get("tool_response", {})
    duration_ms = data.get("duration_ms", "?")
    session_id = data.get("session_id", "unknown")[:8]

    ts = fmt_time()
    header = f"[{ts}][{session_id}][{duration_ms}ms]"

    parts = [header]

    if tool_name == "Bash":
        cmd = tool_input.get("command", "?")
        desc = tool_input.get("description", "")

        exit_tag = ""
        if isinstance(tool_response, dict):
            interrupted = tool_response.get("interrupted", False)
            if interrupted:
                exit_tag = " [INTERRUPTED]"

        if desc:
            parts.append(f" BASH | {desc}")
            parts.append(f"\n  cmd:  {cmd}{exit_tag}")
        else:
            parts.append(f" BASH | {cmd}{exit_tag}")

    elif tool_name == "Write":
        file_path = tool_input.get("file_path", "?")
        parts.append(f" WRITE | {truncate(file_path)}")

    elif tool_name == "Edit":
        file_path = tool_input.get("file_path", "?")
        old = tool_input.get("old_string", "")
        parts.append(f" EDIT  | {truncate(file_path)}")
        if old:
            parts.append(f"\n  old:  {truncate(old, 150)}")

    elif tool_name == "Read":
        file_path = tool_input.get("file_path", "?")
        parts.append(f" READ  | {truncate(file_path)}")

    else:
        parts.append(f" {tool_name}")

    parts.append("\n")

    with open(LOG_FILE, "a", encoding="utf-8") as f:
        f.write("".join(parts))

if __name__ == "__main__":
    main()
