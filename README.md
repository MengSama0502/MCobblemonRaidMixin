# MCobblemonRaidMixin

**[English](#english) | [中文](#chinese)**

---

## English

**MCobblemonRaidMixin** is a multi-platform mod (Fabric & NeoForge) that enhances [Cobblemon](https://modrinth.com/mod/cobblemon) Raid Dens (powered by [Necro's Raid Dens](https://modrinth.com/mod/cobblemon-raids)) with advanced IV (Individual Values) generation controls, spawn blacklists, and tier-based reward command execution.

> **Important:** This is a **Mod** — place it in the `mods/` folder, NOT the `plugins/` folder.

### Supported Platforms

| Platform | Mod ID | Description |
|---|---|---|
| **Fabric** | `mcobbleraidmixinfabric` | Fabric Loader version |
| **NeoForge** | `mcobblemonraidmixin` | NeoForge version |

### Features

- **4 IV Generation Modes** — SET, RANDOM, MIN_MAX, EXACT — each with per-tier (1~7 star) configuration
- **Per-Species IV Overrides** — Define custom IV rules for specific Pokemon species, overriding the global mode
- **Spawn Blacklist** — Prevent legendary/mythical Pokemon from appearing in raid dens
- **Tier-Based Reward Commands** — Execute arbitrary commands (with weighted random selection) when players complete raids
- **Multi-Language Support** — Chinese (zh), English (en), Thai (th)
- **Raid Participant Tracking** — Automatically tracks raid participants for reward distribution

### Requirements

| Dependency | Version |
|---|---|
| Minecraft | 1.21.1 |
| Fabric Loader (Fabric) | >= 0.18.4 |
| NeoForge (NeoForge) | >= 21.1 |
| Cobblemon | 1.7.3+ |
| Cobblemon Raid Dens (Necro) | 0.11.1+ |

### Installation

#### Fabric
1. Download the latest Fabric `.jar` from [Releases](https://github.com/MengSama0502/MCobblemonRaidMixin/releases)
2. Place the `.jar` in your server's `mods/` folder
3. Ensure all dependencies are also in the `mods/` folder
4. Start/restart the server

#### NeoForge
1. Download the latest NeoForge `.jar` from [Releases](https://github.com/MengSama0502/MCobblemonRaidMixin/releases)
2. Place the `.jar` in your server's `mods/` folder
3. Ensure all dependencies are also in the `mods/` folder
4. Start/restart the server

### Configuration

Configuration is done via `config/mcobblemonraidmixin.yml`. The mod supports three languages for the config file: `mcobblemonraidmixin.yml` (default), `mcobblemonraidmixin_zh.yml`, `mcobblemonraidmixin_en.yml`, `mcobblemonraidmixin_th.yml`.

#### IV Generation Modes

| Mode | Description |
|---|---|
| `SET` | Guarantees N perfect IVs (31), remaining stats randomized within [min, max] |
| `RANDOM` | Randomly distributes a total IV sum across all 6 stats |
| `MIN_MAX` | Each stat independently randomized within [min, max] |
| `EXACT` | Precisely specified IV values for each stat |

#### Key Configuration Options

```yaml
language: "zh"                    # Language: zh / en / th
raidIVMode: "SET"                 # Global IV mode

# SET mode — guaranteed perfect IVs per tier (1~7 stars)
guaranteedVPerTier: [0, 1, 1, 1, 2, 2, 3]
minIVPerTier: [0, 0, 0, 0, 0, 0, 0]
maxIVPerTier: [20, 20, 20, 20, 20, 20, 20]

# RANDOM mode — max total IV sum per tier
maxVPerTier: [80, 90, 100, 110, 120, 130, 140]

# MIN_MAX mode — IV range per tier
minIVRangePerTier: [0, 5, 10, 15, 20, 25, 31]
maxIVRangePerTier: [10, 15, 20, 25, 31, 31, 31]

# EXACT mode — exact IVs [HP, ATK, DEF, SP.ATK, SP.DEF, SPD] per tier
exactIVsPerTier:
  - [31, 31, 31, 31, 31, 31]
  # ... (7 tiers)

# Spawn blacklist — these Pokemon will NOT appear in raid dens
spawnBlacklist:
  - "mewtwo"
  - "rayquaza"
  # ...

# Per-species IV overrides (priority over global mode)
overrideExact:
  ditto: [31, 31, 31, 31, 31, 31]
overrideRandom:
  ditto: 120
overrideSet:
  ditto: [0, 2, 20]
overrideMinMax: {}

# Reward commands — executed when a raid is completed
# Placeholders: {player} (player name), {tier} (1~7), {tierIndex} (0~6)
rewardCommandsPerTier:
  Tier1:
    - command: "give {player} cobblemon:rare_candy 1"
      weight: 50
    - command: "give {player} cobblemon:super_rod 1"
      weight: 30
  Tier2: []
  # ...
```

### Reward Command Placeholders

| Placeholder | Description | Example |
|---|---|---|
| `{player}` | Player name | `MengSama` |
| `{tier}` | Raid tier (1~7) | `5` |
| `{tierIndex}` | Raid tier index (0~6) | `4` |

### Language Support

The mod reads language from the config file's `language` field. Supported languages:
- `zh` — Chinese
- `en` — English
- `th` — Thai

Language files are located at `config/mcobblemonraidmixin/lang_*.yml` (auto-generated on first run).

### Building from Source

```bash
git clone https://github.com/MengSama0502/MCobblemonRaidMixin.git
cd MCobblemonRaidMixin
# Place Cobblemon and CobblemonRaidDens jars in libs/

# Build Fabric version
./gradlew :fabric:build
# Output: fabric/build/libs/

# Build NeoForge version
./gradlew :neoforge:build
# Output: neoforge/build/libs/

# Build both
./gradlew build
```

Requires **JDK 21** or newer.

### Project Structure

```
MCobblemonRaidMixin/
├── common/              # Shared code (Config, Lang, Mixins, Participant Tracker)
├── fabric/              # Fabric platform entry point
├── neoforge/            # NeoForge platform entry point
├── build.gradle         # Root build script
├── settings.gradle      # Multi-project settings
└── README.md
```

### License

This project is licensed under the **MIT License** — see below for details.

```
MIT License

Copyright (c) 2025 MengSama0502 & niumadadi520

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

### Authors

- **MengSama0502** — Lead Developer
- **niumadadi520** — Co-Developer

---

## 中文

**MCobblemonRaidMixin** 是一款多平台模组（Fabric & NeoForge），用于增强 [Cobblemon](https://modrinth.com/mod/cobblemon) 的巢穴系统（基于 [Necro's Raid Dens](https://modrinth.com/mod/cobblemon-raids)），提供高级 IV（个体值）生成控制、生成黑名单以及按星级分级的奖励命令执行。

> **重要提示：** 这是一个 **模组**，请放入 `mods/` 文件夹，而非 `plugins/` 文件夹。

### 支持的平台

| 平台 | Mod ID | 说明 |
|---|---|---|
| **Fabric** | `mcobbleraidmixinfabric` | Fabric Loader 版本 |
| **NeoForge** | `mcobblemonraidmixin` | NeoForge 版本 |

### 功能特性

- **4 种 IV 生成模式** — SET、RANDOM、MIN_MAX、EXACT — 每种模式均支持按星级（1~7星）独立配置
- **按物种 IV 覆盖规则** — 为特定宝可梦单独指定 IV 生成方式，优先级高于全局模式
- **生成黑名单** — 禁止传说/幻之宝可梦在巢穴中自然生成
- **按星级奖励命令** — 玩家完成巢穴后可执行自定义命令（支持权重随机选择）
- **多语言支持** — 中文 (zh)、英文 (en)、泰文 (th)
- **巢穴参与者追踪** — 自动追踪巢穴参与者，用于奖励分发

### 环境要求

| 依赖 | 版本 |
|---|---|
| Minecraft | 1.21.1 |
| Fabric Loader（Fabric） | >= 0.18.4 |
| NeoForge（NeoForge） | >= 21.1 |
| Cobblemon | 1.7.3+ |
| Cobblemon Raid Dens (Necro) | 0.11.1+ |

### 安装方法

#### Fabric
1. 从 [Releases](https://github.com/MengSama0502/MCobblemonRaidMixin/releases) 下载最新的 Fabric 版 `.jar`
2. 将 `.jar` 文件放入服务器的 `mods/` 文件夹
3. 确保所有依赖也在 `mods/` 文件夹中
4. 启动/重启服务器

#### NeoForge
1. 从 [Releases](https://github.com/MengSama0502/MCobblemonRaidMixin/releases) 下载最新的 NeoForge 版 `.jar`
2. 将 `.jar` 文件放入服务器的 `mods/` 文件夹
3. 确保所有依赖也在 `mods/` 文件夹中
4. 启动/重启服务器

### 配置说明

配置文件位于 `config/mcobblemonraidmixin.yml`。模组支持三种语言的配置文件：`mcobblemonraidmixin.yml`（默认）、`mcobblemonraidmixin_zh.yml`、`mcobblemonraidmixin_en.yml`、`mcobblemonraidmixin_th.yml`。

#### IV 生成模式

| 模式 | 说明 |
|---|---|
| `SET` | 保证 N 项完美 IV（31），其余项在 [最小, 最大] 区间内随机 |
| `RANDOM` | 将总 IV 和随机分配到 6 项属性中 |
| `MIN_MAX` | 每项属性在 [最小, 最大] 区间内独立随机 |
| `EXACT` | 精确指定每项属性的 IV 值 |

#### 核心配置选项

```yaml
language: "zh"                    # 语言：zh（中文）/ en（英文）/ th（泰文）
raidIVMode: "SET"                 # 全局 IV 生成模式

# SET 模式 — 每个星级保证的完美V数量（1~7星）
guaranteedVPerTier: [0, 1, 1, 1, 2, 2, 3]
minIVPerTier: [0, 0, 0, 0, 0, 0, 0]
maxIVPerTier: [20, 20, 20, 20, 20, 20, 20]

# RANDOM 模式 — 每个星级最大总 IV 和
maxVPerTier: [80, 90, 100, 110, 120, 130, 140]

# MIN_MAX 模式 — 每个星级每项 IV 区间
minIVRangePerTier: [0, 5, 10, 15, 20, 25, 31]
maxIVRangePerTier: [10, 15, 20, 25, 31, 31, 31]

# EXACT 模式 — 精确 IV 值 [HP, 攻击, 防御, 特攻, 特防, 速度] 按星级
exactIVsPerTier:
  - [31, 31, 31, 31, 31, 31]
  # ...（共7个星级）

# 生成黑名单 — 这些宝可梦不会在巢穴中生成
spawnBlacklist:
  - "mewtwo"
  - "rayquaza"
  # ...

# 按物种 IV 覆盖规则（优先级高于全局模式）
overrideExact:
  ditto: [31, 31, 31, 31, 31, 31]
overrideRandom:
  ditto: 120
overrideSet:
  ditto: [0, 2, 20]
overrideMinMax: {}

# 奖励命令 — 完成巢穴后执行
# 占位符：{player}（玩家名）、{tier}（星级 1~7）、{tierIndex}（星级索引 0~6）
rewardCommandsPerTier:
  Tier1:
    - command: "give {player} cobblemon:rare_candy 1"
      weight: 50
    - command: "give {player} cobblemon:super_rod 1"
      weight: 30
  Tier2: []
  # ...
```

### 奖励命令占位符

| 占位符 | 说明 | 示例 |
|---|---|---|
| `{player}` | 玩家名称 | `MengSama` |
| `{tier}` | 巢穴星级（1~7） | `5` |
| `{tierIndex}` | 巢穴星级索引（0~6） | `4` |

### 语言支持

模组从配置文件的 `language` 字段读取语言设置。支持的语言：
- `zh` — 中文
- `en` — 英文
- `th` — 泰文

语言文件位于 `config/mcobblemonraidmixin/lang_*.yml`（首次运行自动生成）。

### 从源码构建

```bash
git clone https://github.com/MengSama0502/MCobblemonRaidMixin.git
cd MCobblemonRaidMixin
# 将 Cobblemon 和 CobblemonRaidDens 的 jar 文件放入 libs/ 目录

# 构建 Fabric 版本
./gradlew :fabric:build
# 输出：fabric/build/libs/

# 构建 NeoForge 版本
./gradlew :neoforge:build
# 输出：neoforge/build/libs/

# 构建全部
./gradlew build
```

需要 **JDK 21** 或更高版本。

### 项目结构

```
MCobblemonRaidMixin/
├── common/              # 共享代码（Config, Lang, Mixins, Participant Tracker）
├── fabric/              # Fabric 平台入口
├── neoforge/            # NeoForge 平台入口
├── build.gradle         # 根构建脚本
├── settings.gradle      # 多项目设置
└── README.md
```

### 开源协议

本项目采用 **MIT 许可证** — 详见上方英文部分。

### 作者

- **MengSama0502** — 主要开发者
- **niumadadi520** — 联合开发者