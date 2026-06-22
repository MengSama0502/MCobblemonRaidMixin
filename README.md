# MCobblemonRaidMixin

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.21.1-brightgreen?style=flat-square" alt="Minecraft 1.21.1">
  <img src="https://img.shields.io/badge/Loader-Fabric_|_NeoForge-blue?style=flat-square" alt="Fabric | NeoForge">
  <img src="https://img.shields.io/badge/License-MIT-yellow?style=flat-square" alt="MIT License">
  <img src="https://img.shields.io/badge/Version-1.1.0-orange?style=flat-square" alt="Version 1.1.0">
  <img src="https://img.shields.io/badge/JDK-21-red?style=flat-square" alt="JDK 21">
</p>

<p align="center">
  <strong>Advanced Raid Dens Enhancement for Cobblemon</strong>
  <br>
  Precision IV control · Spawn blacklist · Weighted reward system · Multi-language
</p>

---

## English

### Overview

MCobblemonRaidMixin is a mod for [Cobblemon](https://modrinth.com/mod/cobblemon) that significantly enhances the Raid Dens experience powered by [Necro's Raid Dens](https://modrinth.com/mod/cobblemon-raids). It provides fine-grained control over raid reward Pokemon's IV generation, the ability to blacklist specific species from spawning, and a flexible weighted command reward system — all configurable per raid tier. Works on both **server and client**, and can be used independently on either side.

### Platform Support

| Platform | Mod ID | Archive Name |
|---|---|---|
| **Fabric** | `mcobbleraidmixinfabric` | `MCobbleRaidMixinFabric-1.1.0.jar` |
| **NeoForge** | `mcobblemonraidmixin` | `MCobbleRaidMixinNeoForge-1.1.0.jar` |

### Dependencies

| Dependency | Minimum Version |
|---|---|
| Minecraft | 1.21.1 |
| Fabric Loader | 0.18.4+ *(Fabric only)* |
| NeoForge | 21.1+ *(NeoForge only)* |
| Cobblemon | 1.7.3+ |
| Cobblemon Raid Dens (Necro) | 0.11.1+ |

### Installation

#### Server
1. Download the appropriate JAR for your platform from [Releases](https://github.com/MengSama0502/MCobblemonRaidMixin/releases)
2. Place the JAR into your server's `mods/` directory
3. Ensure all dependencies above are also present in `mods/`
4. Start the server — the default configuration will be auto-generated at `config/mcobblemonraidmixin.yml`

#### Client (Single Player)
1. Download the appropriate JAR for your platform from [Releases](https://github.com/MengSama0502/MCobblemonRaidMixin/releases)
2. Place the JAR into your client's `mods/` directory
3. Ensure all dependencies above are also present in `mods/`
4. Launch the game — the default configuration will be auto-generated at `config/mcobblemonraidmixin.yml`

> **Note:** The mod works independently on both sides. You can install it on the server only, client only, or both.

### Core Features

#### IV Generation Engine

Four distinct IV generation modes, each with independent per-tier (1-7 star) parameters:

| Mode | Behavior |
|---|---|
| `SET` | Guarantees *N* perfect IVs (31). Remaining stats are randomized within a configurable [min, max] range. |
| `RANDOM` | Randomly distributes a total IV budget across all 6 stats using a proportional allocation algorithm. |
| `MIN_MAX` | Each stat is independently randomized within a defined [min, max] interval. |
| `EXACT` | Precisely specifies all 6 IV values per tier. |

#### Per-Species Override System

Override IV generation rules for specific Pokemon species, taking priority over the global mode. Each override supports all four IV modes independently:

```yaml
overrideSet:
  mewtwo: [10, 3, 31]        # [min, guaranteedV, max]
overrideRandom:
  ditto: 120                  # max total IV sum
overrideExact:
  ditto: [31, 31, 31, 31, 31, 31]
overrideMinMax:
  pikachu: [15, 31]          # [min, max] per stat
```

#### Spawn Blacklist

Prevent specific Pokemon species from appearing in raid dens. Supports both `cobblemon:name` and shorthand `name` formats:

```yaml
spawnBlacklist:
  - "mewtwo"
  - "rayquaza"
  - "cobblemon:arceus"
```

#### Weighted Reward Command System

Execute arbitrary commands with weighted random selection when players complete raid dens. Each tier (1-7) can have its own set of reward commands:

```yaml
rewardCommandsPerTier:
  Tier1:
    - command: "give {player} cobblemon:rare_candy 1"
      weight: 50
    - command: "give {player} cobblemon:super_rod 1"
      weight: 30
    - command: "give {player} cobblemon:luxury_ball 3"
      weight: 20
```

| Placeholder | Expands To | Example |
|---|---|---|
| `{player}` | Player name | `MengSama` |
| `{tier}` | Raid tier (1-7) | `5` |
| `{tierIndex}` | Zero-based tier index | `4` |

#### Raid Participant Tracking

Automatically tracks all players who join a raid. When the raid boss is defeated, rewards are distributed to every participant — not just the player who dealt the final blow.

### Configuration Reference

> Default path: `config/mcobblemonraidmixin.yml`

```yaml
language: "zh"
raidIVMode: "SET"

# ── SET Mode ──────────────────────────────────
guaranteedVPerTier: [0, 1, 1, 1, 2, 2, 3]
minIVPerTier:       [0, 0, 0, 0, 0, 0, 0]
maxIVPerTier:       [20, 20, 20, 20, 20, 20, 20]

# ── RANDOM Mode ───────────────────────────────
maxVPerTier:        [80, 90, 100, 110, 120, 130, 140]

# ── MIN_MAX Mode ──────────────────────────────
minIVRangePerTier:  [0, 5, 10, 15, 20, 25, 31]
maxIVRangePerTier:  [10, 15, 20, 25, 31, 31, 31]

# ── EXACT Mode ────────────────────────────────
exactIVsPerTier:
  - [31, 31, 31, 31, 31, 31]
  - [31, 31, 31, 31, 31, 31]
  - [31, 31, 31, 31, 31, 31]
  - [31, 31, 31, 31, 31, 31]
  - [31, 31, 31, 31, 31, 31]
  - [31, 31, 31, 31, 31, 31]
  - [31, 31, 31, 31, 31, 31]

spawnBlacklist: []
overrideExact: {}
overrideRandom: {}
overrideSet: {}
overrideMinMax: {}
rewardCommandsPerTier: {}
```

> All array values correspond to raid tiers 1 through 7 (index 0 = 1-star, index 6 = 7-star).

### Localization

| Code | Language | Config File |
|---|---|---|
| `zh` | Chinese | `mcobblemonraidmixin_zh.yml` |
| `en` | English | `mcobblemonraidmixin_en.yml` |
| `th` | Thai | `mcobblemonraidmixin_th.yml` |

Language files are auto-generated in `config/mcobblemonraidmixin/lang_*.yml` on first run. Set `language` in the config to switch.

### Project Architecture

```
MCobblemonRaidMixin/
├── common/                          # Platform-agnostic shared code
│   └── src/main/java/
│       └── MengSama/Mod/mcobblemonraidmixin/
│           ├── Config.java          # YAML configuration loader
│           ├── Lang.java            # Multi-language i18n engine
│           ├── RaidParticipantTracker.java
│           └── mixin/
│               ├── RaidBossMixin.java       # IV injection + reward dispatch
│               └── RaidRegistryMixin.java   # Spawn blacklist filter
├── fabric/                          # Fabric loader entry point
│   ├── build.gradle                 # Fabric Loom
│   └── src/main/
│       ├── java/.../fabric/Mcobblemonraidmixinfabric.java
│       └── resources/
│           ├── fabric.mod.json
│           └── mcobbleraidmixinfabric.mixins.json
├── neoforge/                        # NeoForge loader entry point
│   ├── build.gradle                 # NeoForge ModDev
│   └── src/main/
│       ├── java/.../neoforge/Mcobblemonraidmixin.java
│       ├── resources/mcobblemonraidmixin.mixins.json
│       └── templates/META-INF/neoforge.mods.toml
├── build.gradle                     # Root project
├── settings.gradle                  # Multi-module configuration
└── README.md
```

### Building from Source

**Prerequisites:** JDK 21+

```bash
git clone https://github.com/MengSama0502/MCobblemonRaidMixin.git
cd MCobblemonRaidMixin

# Place dependency JARs in libs/:
#   - Cobblemon-fabric-1.7.3+1.21.1.jar
#   - cobblemonraiddens-fabric-0.11.1+1.21.1.jar

# Build Fabric
./gradlew :fabric:build
# Output → fabric/build/libs/MCobbleRaidMixinFabric-1.1.0.jar

# Build NeoForge
./gradlew :neoforge:build
# Output → neoforge/build/libs/MCobbleRaidMixinNeoForge-1.1.0.jar

# Build both
./gradlew build
```

### FAQ

**Q: Does this mod work on the client?**
A: Yes. The mod works on both server and client independently. You can use it on the server for multiplayer, on the client for single player, or on both.

**Q: Can I use both Fabric and NeoForge versions on the same server?**
A: No. Choose the version matching your server's mod loader. The functionality is identical.

**Q: What happens if I don't configure reward commands?**
A: The mod simply applies IV generation and blacklist filtering. Reward commands are optional.

**Q: How does the weighted random selection work?**
A: Each command has a `weight` value. The probability of a command being selected = `weight / totalWeight`. Commands with higher weights are more likely to be chosen.

**Q: Are override rules applied before or after the global mode?**
A: Per-species overrides take full priority. If an override exists for a species, the global mode is completely bypassed for that Pokemon.

### License

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

**MengSama0502** — Lead Developer
**niumadadi520** — Co-Developer

---

## 中文

### 概述

MCobblemonRaidMixin 是一款面向 [Cobblemon](https://modrinth.com/mod/cobblemon) 的模组，基于 [Necro's Raid Dens](https://modrinth.com/mod/cobblemon-raids) 深度增强巢穴玩法。提供精细化的奖励宝可梦 IV 生成控制、物种黑名单过滤以及灵活的权重随机命令奖励系统——全部按巢穴星级独立配置。支持**服务端和客户端**，可独立在任意一侧使用。

### 平台支持

| 平台 | Mod ID | 输出文件 |
|---|---|---|
| **Fabric** | `mcobbleraidmixinfabric` | `MCobbleRaidMixinFabric-1.1.0.jar` |
| **NeoForge** | `mcobblemonraidmixin` | `MCobbleRaidMixinNeoForge-1.1.0.jar` |

### 依赖要求

| 依赖 | 最低版本 |
|---|---|
| Minecraft | 1.21.1 |
| Fabric Loader | 0.18.4+ *(仅 Fabric)* |
| NeoForge | 21.1+ *(仅 NeoForge)* |
| Cobblemon | 1.7.3+ |
| Cobblemon Raid Dens (Necro) | 0.11.1+ |

### 安装步骤

#### 服务端
1. 从 [Releases](https://github.com/MengSama0502/MCobblemonRaidMixin/releases) 下载对应平台的 JAR 文件
2. 放入服务器 `mods/` 目录
3. 确保上述所有依赖也存在于 `mods/` 中
4. 启动服务器——默认配置将自动生成于 `config/mcobblemonraidmixin.yml`

#### 客户端（单人游戏）
1. 从 [Releases](https://github.com/MengSama0502/MCobblemonRaidMixin/releases) 下载对应平台的 JAR 文件
2. 放入客户端 `mods/` 目录
3. 确保上述所有依赖也存在于 `mods/` 中
4. 启动游戏——默认配置将自动生成于 `config/mcobblemonraidmixin.yml`

> **注意：** 模组在两侧均可独立运行。你可以仅安装在服务端、仅安装在客户端，或两侧同时安装。

### 核心功能

#### IV 生成引擎

提供四种 IV 生成模式，每种模式均支持按 1-7 星独立参数配置：

| 模式 | 行为 |
|---|---|
| `SET` | 保证 *N* 项完美 IV（31），其余项在 [最小, 最大] 区间内随机 |
| `RANDOM` | 将总 IV 预算按比例随机分配到 6 项属性中 |
| `MIN_MAX` | 每项属性在 [最小, 最大] 区间内独立随机 |
| `EXACT` | 精确指定每星级 6 项 IV 值 |

#### 按物种覆盖系统

为特定宝可梦单独指定 IV 生成规则，优先级高于全局模式。每种覆盖独立支持全部四种 IV 模式：

```yaml
overrideSet:
  mewtwo: [10, 3, 31]        # [最小, 保底V, 最大]
overrideRandom:
  ditto: 120                  # 最大总 IV 和
overrideExact:
  ditto: [31, 31, 31, 31, 31, 31]
overrideMinMax:
  pikachu: [15, 31]          # [最小, 最大] 每项
```

#### 生成黑名单

阻止特定宝可梦在巢穴中生成。支持 `cobblemon:name` 和简写 `name` 两种格式：

```yaml
spawnBlacklist:
  - "mewtwo"
  - "rayquaza"
  - "cobblemon:arceus"
```

#### 权重随机奖励命令

玩家完成巢穴后执行自定义命令，支持权重随机选择。每个星级（1-7）可独立配置奖励池：

```yaml
rewardCommandsPerTier:
  Tier1:
    - command: "give {player} cobblemon:rare_candy 1"
      weight: 50
    - command: "give {player} cobblemon:super_rod 1"
      weight: 30
    - command: "give {player} cobblemon:luxury_ball 3"
      weight: 20
```

| 占位符 | 替换为 | 示例 |
|---|---|---|
| `{player}` | 玩家名称 | `MengSama` |
| `{tier}` | 巢穴星级 (1-7) | `5` |
| `{tierIndex}` | 星级索引 (0-6) | `4` |

#### 参与者追踪

自动记录所有加入巢穴的玩家。巢穴首领被击败后，奖励将分发给所有参与者——而非仅限最后一击的玩家。

### 配置参考

> 默认路径：`config/mcobblemonraidmixin.yml`

```yaml
language: "zh"
raidIVMode: "SET"

# ── SET 模式 ──────────────────────────────────
guaranteedVPerTier: [0, 1, 1, 1, 2, 2, 3]
minIVPerTier:       [0, 0, 0, 0, 0, 0, 0]
maxIVPerTier:       [20, 20, 20, 20, 20, 20, 20]

# ── RANDOM 模式 ───────────────────────────────
maxVPerTier:        [80, 90, 100, 110, 120, 130, 140]

# ── MIN_MAX 模式 ──────────────────────────────
minIVRangePerTier:  [0, 5, 10, 15, 20, 25, 31]
maxIVRangePerTier:  [10, 15, 20, 25, 31, 31, 31]

# ── EXACT 模式 ────────────────────────────────
exactIVsPerTier:
  - [31, 31, 31, 31, 31, 31]
  - [31, 31, 31, 31, 31, 31]
  - [31, 31, 31, 31, 31, 31]
  - [31, 31, 31, 31, 31, 31]
  - [31, 31, 31, 31, 31, 31]
  - [31, 31, 31, 31, 31, 31]
  - [31, 31, 31, 31, 31, 31]

spawnBlacklist: []
overrideExact: {}
overrideRandom: {}
overrideSet: {}
overrideMinMax: {}
rewardCommandsPerTier: {}
```

> 所有数组值按索引 0-6 对应 1-7 星巢穴。

### 多语言

| 代码 | 语言 | 配置文件 |
|---|---|---|
| `zh` | 中文 | `mcobblemonraidmixin_zh.yml` |
| `en` | 英文 | `mcobblemonraidmixin_en.yml` |
| `th` | 泰文 | `mcobblemonraidmixin_th.yml` |

语言文件在首次运行时自动生成于 `config/mcobblemonraidmixin/lang_*.yml`。修改配置中的 `language` 字段即可切换。

### 项目架构

```
MCobblemonRaidMixin/
├── common/                          # 平台无关共享代码
│   └── src/main/java/
│       └── MengSama/Mod/mcobblemonraidmixin/
│           ├── Config.java          # YAML 配置加载器
│           ├── Lang.java            # 多语言 i18n 引擎
│           ├── RaidParticipantTracker.java
│           └── mixin/
│               ├── RaidBossMixin.java       # IV 注入 + 奖励分发
│               └── RaidRegistryMixin.java   # 生成黑名单过滤
├── fabric/                          # Fabric 加载器入口
│   ├── build.gradle                 # Fabric Loom
│   └── src/main/
│       ├── java/.../fabric/Mcobblemonraidmixinfabric.java
│       └── resources/
│           ├── fabric.mod.json
│           └── mcobbleraidmixinfabric.mixins.json
├── neoforge/                        # NeoForge 加载器入口
│   ├── build.gradle                 # NeoForge ModDev
│   └── src/main/
│       ├── java/.../neoforge/Mcobblemonraidmixin.java
│       ├── resources/mcobblemonraidmixin.mixins.json
│       └── templates/META-INF/neoforge.mods.toml
├── build.gradle                     # 根项目
├── settings.gradle                  # 多模块配置
└── README.md
```

### 从源码构建

**前置要求：** JDK 21+

```bash
git clone https://github.com/MengSama0502/MCobblemonRaidMixin.git
cd MCobblemonRaidMixin

# 将依赖 JAR 放入 libs/ 目录：
#   - Cobblemon-fabric-1.7.3+1.21.1.jar
#   - cobblemonraiddens-fabric-0.11.1+1.21.1.jar

# 构建 Fabric
./gradlew :fabric:build
# 输出 → fabric/build/libs/MCobbleRaidMixinFabric-1.1.0.jar

# 构建 NeoForge
./gradlew :neoforge:build
# 输出 → neoforge/build/libs/MCobbleRaidMixinNeoForge-1.1.0.jar

# 构建全部
./gradlew build
```

### 常见问题

**Q: 此模组可以在客户端使用吗？**
A: 可以。模组在服务端和客户端均可独立运行。可以在服务端用于多人游戏，在客户端用于单人游戏，或两侧同时使用。

**Q: 能在同一服务器上同时使用 Fabric 和 NeoForge 版本吗？**
A: 不能。请选择与服务器 Mod Loader 匹配的版本。两者功能完全一致。

**Q: 不配置奖励命令会怎样？**
A: 模组仅执行 IV 生成和黑名单过滤。奖励命令是可选的。

**Q: 权重随机是如何工作的？**
A: 每条命令有权重值 `weight`。选中概率 = `weight / totalWeight`。权重越高的命令越容易被选中。

**Q: 覆盖规则是应用于全局模式之前还是之后？**
A: 按物种覆盖具有完全优先权。如果某物种存在覆盖规则，则该宝可梦完全跳过全局模式。

### 开源协议

本项目采用 **MIT 许可证**。详见上方英文 License 部分。

### 作者

**MengSama0502** — 主要开发者
**niumadadi520** — 联合开发者