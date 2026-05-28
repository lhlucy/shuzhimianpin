# Git 提交信息（Commit Message）规范

> 本文件为本地提交规则说明，已加入 `.gitignore`，不上传远程仓库。

---

# 本项目执行规则

后续所有 Git 提交必须遵守以下要求：

- 提交信息必须使用中文描述。
- Header 必须采用 Conventional Commits 格式：`<type>(<scope>): <subject>`。
- `type` 使用规范类型，例如 `feat`、`fix`、`docs`、`chore`、`refactor` 等。
- `scope` 使用中文或明确模块名，例如 `项目`、`仓库`、`后端`、`前端`、`文档`。
- `subject` 用一句简短中文说明本次提交目的，结尾不加句号。
- Body 用中文说明为什么修改、修改了什么、是否有风险或注意事项。
- 不提交依赖目录、构建产物、日志、上传文件、数据库转储、真实密钥与本规则文件。

本次已执行的提交信息：

```text
feat(项目): 初始化数智面聘项目

提交前后端源码、数据库迁移脚本、技术文档和项目截图资源。

补充根目录忽略规则，排除依赖、构建产物、日志、上传文件、数据库转储和提交规范文件。

将本地明文运行凭据改为环境变量占位，并提供环境变量示例，降低远程仓库泄露风险。
```

```text
chore(仓库): 合并远程初始提交

保留远程仓库已有 LICENSE 文件，并将本地项目初始化提交接入同一 main 历史。
```

---

Git 提交信息（Commit Message）的标准格式通常遵循 **Conventional Commits** 规范。这是一种被广泛采用的社区标准（如 Angular、Vue、React 等知名项目都在使用），它能清晰地表达提交意图，并有助于自动生成变更日志（Changelog）和自动化版本管理。

---

# 1. 标准格式结构

一条标准的 Commit Message 由三部分组成：

- Header（必填）
- Body（可选）
- Footer（可选）

```text
<type>(<scope>): <subject>

<body>

<footer>
```

---

## 详细拆解

### 1. Header（标题行）

```text
<type>(<scope>): <subject>
```

#### type（必填）

提交的类型，表明这次提交的性质（见下文类型列表）。

#### scope（可选）

影响范围，用括号包裹。通常是模块名、文件名或功能区域。

例如：

- auth
- ui
- db
- api

#### subject（必填）

简短的描述。

要求：

- 以动词开头，使用祈使句（如 “add” 而不是 “added” 或 “adds”）
- 首字母小写
- 结尾不加句号
- 长度建议不超过 50 个字符

---

### 2. Body（正文）

要求：

- 与 Header 之间空一行
- 详细描述为什么做这个修改，而不仅仅是做了什么
- 可以包含多段
- 每行建议不超过 72 个字符（方便终端查看）

---

### 3. Footer（页脚）

要求：

- 与 Body 之间空一行

主要用于两种情况：

#### 破坏性变更（Breaking Changes）

以 `BREAKING CHANGE:` 开头，描述不兼容的修改。

#### 关联问题

引用 Issue 或 Bug 编号，例如：

```text
Closes #123
Fixes #456
```

---

# 2. Type（提交类型）列表

这是最关键的部分，常用类型如下：

| 类型     | 含义                      | 示例                                    |
| -------- | ------------------------- | --------------------------------------- |
| feat     | 新增功能（Feature）       | `feat(user): 添加用户登录功能`          |
| fix      | 修复 Bug（Bug Fix）       | `fix(auth): 修复令牌过期导致的登录失败` |
| docs     | 文档变更（Documentation） | `docs: 更新 API 接口文档`               |
| style    | 格式调整（Style）         | `style: 删除多余空格，格式化代码`       |
| refactor | 代码重构（Refactoring）   | `refactor(db): 重构数据库连接池逻辑`    |
| perf     | 性能优化（Performance）   | `perf: 优化图片加载速度`                |
| test     | 测试相关（Tests）         | `test: 增加用户注册流程的单元测试`      |
| chore    | 构建/工具杂项（Chores）   | `chore: 升级依赖包版本`                 |
| ci       | CI/CD 配置                | `ci: 修改 GitHub Actions 构建脚本`      |
| revert   | 回退提交                  | `revert: 回退 "feat: 添加支付功能"`     |

---

# 3. 示例

## ✅ 优秀示例

---

## 示例 1：简单的新功能

```text
feat(cart): 添加购物车商品数量增减功能

实现了前端按钮点击事件，后端同步更新数据库库存。
增加了边界检查，防止数量小于 1。

Closes #1024
```

---

## 示例 2：修复 Bug

```text
fix(api): 修复高并发下订单状态不一致的问题

在分布式锁释放前增加了重试机制，
防止因网络波动导致的锁未释放。

Fixes #897
```

---

## 示例 3：破坏性变更（Breaking Change）

```text
refactor(auth): 移除旧的 JWT 验证逻辑

BREAKING CHANGE: 移除了 `validateTokenOld` 方法，
所有调用方需迁移至 `validateTokenV2`。
```

---

# 4. 为什么要遵守这个规范？

## 1. 快速理解历史

通过：

```bash
git log --oneline
```

一眼就能看出哪些是功能开发，哪些是修 Bug。

---

## 2. 自动化生成日志

工具（如 `standard-version`、`semantic-release`）可以自动扫描 commit 历史，生成漂亮的 `CHANGELOG.md`。

---

## 3. 自动版本号管理

- `feat` -> 触发 Minor 版本升级（1.1.0 -> 1.2.0）
- `fix` -> 触发 Patch 版本升级（1.1.0 -> 1.1.1）
- `BREAKING CHANGE` -> 触发 Major 版本升级（1.1.0 -> 2.0.0）

---

## 4. 代码审查（Code Review）

审查者能够更快理解提交意图。

---

# 5. 辅助工具推荐

如果你手动写觉得麻烦或容易忘，可以使用以下工具强制规范：

---

## Commitizen

一个命令行工具，通过交互式问答引导你生成标准的 commit message。

```bash
npx commitizen
```

---

## Husky + Commitlint

在 Git Hook 中拦截不符合规范的提交，强制要求团队遵守标准。

例如：

如果你写了：

```bash
git commit -m "fix bug"
```

它会报错并拒绝提交，提示你格式错误。

---

# 总结速查

```text
类型(范围): 动词开头短句

空一行

详细描述为什么

空一行

关联 Issue 或 破坏性说明
```

---

## 示例

```text
feat(auth): add google login support
```
