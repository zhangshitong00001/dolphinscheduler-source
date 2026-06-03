# Claude Code Web UI 方案

## 目标

在浏览器上操作 Claude Code，跟 Linux 终端里一样的体验，且更方便：
- 选择模式（Normal / Auto / Plan）
- 配置参数（model、effort、max-turns）
- 实时看输出日志，跟终端一模一样
- 管理后台会话（启动、attach、detach、kill）
- 浏览项目文件、查看 git 状态

## 技术选型

| 层 | 技术 | 原因 |
|---|------|------|
| 后端 | **FastAPI + WebSocket** | 实时流式输出、启动/管理 claude 子进程 |
| 终端模拟 | **Xterm.js** | 浏览器里跑完整终端，跟 SSH 一样 |
| 前端 | **Vue 3 + 原生 JS** | 轻量、不需要复杂框架 |
| 进程管理 | **asyncio subprocess + PTY** | 通过伪终端驱动 claude CLI |
| 会话存储 | **SQLite** | 轻量、记录会话历史和输出 |

## 架构

```
浏览器 (Vue3 + Xterm.js)
    │
    ├── WebSocket ──→ FastAPI ──→ claude 子进程 (PTY)
    │   (实时流)        │              │
    │                   │          stdout/stderr
    │                   │              │
    ├── REST API ─────→ │              └── → 显示在浏览器终端
    │   (配置/管理)       │
    │                   ├── SQLite (会话记录)
    │                   └── 文件系统 (项目文件)
```

## 功能模块

### 1. 终端面板（核心）
- Xterm.js 全功能终端模拟
- 支持 ANSI 颜色、光标移动、Ctrl 快捷键
- 输入框直接输入命令，回车发给 claude
- 实时显示 claude 输出（工具调用、代码 diff、git 日志）

### 2. 模式 & 参数面板（侧栏/顶栏）

| 参数 | 控件 | 选项 |
|------|------|------|
| 模式 | 下拉框 | Normal / Auto / Plan / AcceptEdits |
| Model | 下拉框 | Sonnet / Opus / Haiku |
| Effort | 滑块 | Low / Medium / High / Max |
| Max Turns | 数字输入 | 1-100 |
| 权限 | 开关 | --dangerously-skip-permissions |
| 项目目录 | 路径输入 | 可切换项目 |

### 3. 会话管理
- 左侧栏：会话列表（运行中 / 已完成 / Needs Input）
- 每个会话显示：状态、耗时、模型、模式
- 操作：attach、detach、kill、重命名
- 点击会话恢复历史输出

### 4. 项目文件浏览
- 文件树：浏览项目结构
- 点击文件：只读查看内容
- 快速查看：CLAUDE.md、settings.json、git status

### 5. Git 快捷操作
- 查看 git status、diff
- 一键提交 + push
- 创建 PR 按钮

## 页面布局

```
┌─────────────────────────────────────────────────┐
│  [Logo]  Claude Code Web               [设置]   │
├──────────┬──────────────────────────────────────┤
│          │                                      │
│ 会话列表  │       Xterm.js 终端                   │
│          │                                      │
│ ● 运行中  │  $ claude -bp "..."                 │
│ ○ 已完成  │  [Read] auth.py                     │
│ ○ 已完成  │  [Write] SessionManager.java        │
│          │  [Bash] git add ...                  │
│          │  ✓ 提交完成                           │
│          │                                      │
│          │                                      │
│          │  ┌─ 输入框 ──────────────────────┐   │
│          │  │ >                             │   │
│          │  └────────────────────────────────┘   │
├──────────┴──────────────────────────────────────┤
│ [模式: Auto ▼] [模型: Sonnet ▼] [Effort: 🎚]   │
│ [Max Turns: 30] [⚠ 跳过权限] [项目: dolphin...] │
└─────────────────────────────────────────────────┘
```

## 接口设计

### WebSocket (实时流)
```
ws://host:8000/ws/{session_id}

客户端 → 服务端:
  {"type": "input", "data": "完成 SessionManager 实现"}
  {"type": "ctrl_c"}
  {"type": "resize", "cols": 120, "rows": 40}

服务端 → 客户端:
  {"type": "output", "data": "\x1b[32m[Write]\x1b[0m RedisConfig.java"}
  {"type": "status", "state": "running|done|waiting_input"}
  {"type": "error", "message": "..."}
```

### REST API
```
POST /api/session/start     # 启动新会话
POST /api/session/stop      # 停止会话
GET  /api/session/list      # 列出所有会话
GET  /api/session/{id}      # 会话详情 + 历史

GET  /api/project/files     # 浏览项目文件
GET  /api/project/git/status # git 状态

POST /api/config/apply      # 应用配置文件设置
GET  /api/config            # 获取当前配置
```

## 后端核心逻辑

```python
# 伪终端驱动 claude 的关键代码
import asyncio
import pty
import os

class ClaudeSession:
    def __init__(self, mode, model, max_turns, project_dir):
        self.mode = mode
        self.process = None
        self.fd = None  # PTY file descriptor
    
    async def start(self, initial_prompt=""):
        # 创建伪终端
        master_fd, slave_fd = pty.openpty()
        self.fd = master_fd
        
        # 构建命令
        cmd = ["claude"]
        if self.mode == "auto":
            cmd += ["--permission-mode", "auto"]
        elif self.mode == "plan":
            cmd += ["--permission-mode", "plan"]
        elif self.mode == "accept":
            cmd += ["--permission-mode", "acceptEdits"]
        
        # 启动子进程，连接到伪终端
        self.process = await asyncio.create_subprocess_exec(
            *cmd,
            stdin=slave_fd,
            stdout=slave_fd,
            stderr=slave_fd,
            cwd=self.project_dir,
        )
        
        # 读取 PTY 输出并通过 WebSocket 推送
        loop.create_task(self._read_output())
    
    async def _read_output(self):
        loop = asyncio.get_event_loop()
        while True:
            data = await loop.run_in_executor(None, os.read, self.fd, 4096)
            if not data:
                break
            await self.websocket.send_json({
                "type": "output",
                "data": data.decode("utf-8", errors="replace")
            })
```

## 需要安装的依赖

```bash
# 后端
pip install fastapi uvicorn websockets
# 前端不需要额外安装 (Vue 3 CDN + Xterm.js CDN)
```

## 文件清单

```
claude-web-ui/
├── main.py                    # FastAPI 后端入口
├── session_manager.py         # Claude 子进程管理
├── static/
│   ├── index.html             # 主页面
│   ├── app.js                 # Vue 3 应用逻辑
│   └── style.css              # 样式（暗黑主题）
└── claude_web.db              # SQLite 会话存储（自动创建）
```

## 风险和注意事项

1. **PTY 兼容性** — Xterm.js + PTY 的交互需要测试，特别是 Ctrl+C、Tab 补全等
2. **Claude 的对话框** — 首次使用时 workspace trust 和权限确认弹窗需要处理
3. **权限隔离** — 后端用 `claude-user` 身份运行，避免 root 限制
4. **安全性** — Web UI 需要加简单的密码保护，否则任何人都能操作服务器
5. **网络** — 如果服务器没有公网 IP，需要通过 Nginx 反向代理暴露

## 后续可扩展

- ✅ 多项目切换
- ✅ 会话历史回放（replay）
- ✅ 文件编辑器（直接在网页上编辑代码）
- ✅ 多人协作（分享会话给团队）
- ✅ 手机端适配（响应式）
