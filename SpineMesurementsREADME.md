# 脊柱正位片指标计算说明
基于关键点 JSON 与测量结果 JSON 的几何推导

本文把AI测量和AI检测的响应JSON对齐起来，说明字段含义，并给出用这些关键点计算以下指标的做法与公式：

- Cobb Angle（CA）
- T1 Tilt
- Pelvic Tilt（Pelvic）
- Sacral Angle（Sacral）
- AVT
- Trunk Shift（TS）

---

## 0. 坐标系与单位

你的 JSON 坐标来自图像像素坐标系：

- `x` 向右增大
- `y` 向下增大
- 距离单位默认是 **像素 px**
- 若要换算成 **毫米 mm**，需要 DICOM 的 `PixelSpacing`（mm/px）。当前 JSON 未提供，所以 AVT/TS 默认输出 px, 由于实际使用时有标准距离，所以可以计算毫米

图像尺寸字段：

- `imageWidth`, `imageHeight`

---

## 1. 输入数据结构与字段含义

### 1.1 `pose_keypoints` 姿态关键点

你提供的关键点：

- `CR`, `CL`：上躯干点（常用于肩带/锁骨线）
- `IR`, `IL`：左右髂嵴最高点（Iliac crest）
- `SR`, `SL`：左右骶骨基底点（Sacral base）

每个点结构：

- `x`, `y`：像素坐标
- `confidence`：检测置信度

用途映射：

| 字段 | 含义（几何） | 主要用于 |
|---|---|---|
| CR-CL | 上躯干水平参考线 | 可用于上躯干倾斜（你当前的测量里未直接用） |
| IR-IL | 骨盆水平线 | Pelvic Tilt |
| SR-SL | 骶骨基底线 | Sacral Angle、CSVL |

---

### 1.2 `vertebrae` 椎体框与派生点

每个椎体（如 `T1`, `C7`, `L5`）包含 `corners`：

- `top_left`, `top_right`：上终板左右角点
- `bottom_left`, `bottom_right`：下终板左右角点
- `top_mid`：上终板中点
- `bottom_mid`：下终板中点
- `center`：椎体几何中心

以及：

- `confidence`：该椎体检测置信度
- `class_id`：类别编号映射

用途映射：

| 椎体字段 | 含义（几何） | 主要用于 |
|---|---|---|
| T1.top_left/top_right | T1 上终板线 | T1 Tilt |
| 各椎体 top/bottom 终板线 | 椎体终板方向 | Cobb Angle 终椎选择与角度 |
| C7.center | C7 垂线基准点 | Trunk Shift（C7PL） |
| 各椎体 center | 椎体中心点 | AVT 顶椎选择与偏移 |

---

### 1.3 `measurements` 测量结果结构（输出）

你给的测量结果里，每个指标一个对象：

- `type`：指标名（`T1 Tilt`, `CA`, `Pelvic`, `Sacral`, `AVT`, `TS`）
- `points`：用于该指标的两点（通常是一条线段或一段水平距离的两端点）
- `angle`：角度指标会填角度；平移指标可能不填或不叫 angle

注意：`measurements.points` 更像“可视化画线用的点”，实际计算建议直接用 `pose_keypoints` + `vertebrae` 里的点，避免同名字段在不同版本里不一致。

---

## 2. 通用几何公式

### 2.1 两点确定直线的倾斜角（相对水平线）

给定两点 A(x1,y1), B(x2,y2)：

- 方向角（弧度）：
  \[
  \theta = \operatorname{atan2}(y_2-y_1,\ x_2-x_1)
  \]
- 转成角度（度）：
  \[
  \theta^\circ = \theta \cdot \frac{180}{\pi}
  \]

为了可读性，常把角度规约到 [-90°, 90°]：

\[
\theta^\circ \leftarrow \left((\theta^\circ + 90) \bmod 180\right) - 90
\]

### 2.2 两条直线的锐角夹角（用于 Cobb）

已知两条线倾角 `θ1`, `θ2`：

\[
\Delta = |\theta_1-\theta_2|
\]
\[
\Delta > 180 \Rightarrow \Delta = 360 - \Delta
\]
\[
\Delta > 90 \Rightarrow \Delta = 180 - \Delta
\]

最终 `Δ` 为 0–90° 的锐角。

### 2.3 点到垂直参考线的水平距离（用于 AVT/TS）

垂直线 `x = x0`，点 `P(x,y)`：

- 带符号距离：`d = x - x0`
- 无符号距离：`|d|`

---

## 3. 各指标计算方法

### 3.1 T1 Tilt

**定义**：T1 上终板线相对水平线的倾斜角。

**取点**（来自 `vertebrae["T1"].corners`）：

- `A = top_left`
- `B = top_right`

**公式**：

\[
T1Tilt = angle(A,B)
\]

对应你的测量结果 `measurements.type = "T1 Tilt"`，其中 `points` 正是 T1 上终板两点。

---

### 3.2 Pelvic Tilt（Pelvic）

**定义**：左右髂嵴连线相对水平线倾斜角。

**取点**（来自 `pose_keypoints`）：

- `A = IR`
- `B = IL`

**公式**：

\[
PelvicTilt = angle(IR, IL)
\]

对应 `measurements.type = "Pelvic"`。

---

### 3.3 Sacral Angle（Sacral）

**定义**：骶骨基底线相对水平线倾斜角。

**取点**（来自 `pose_keypoints`）：

- `A = SR`
- `B = SL`

**公式**：

\[
SacralAngle = angle(SR, SL)
\]

对应 `measurements.type = "Sacral"`。

---

### 3.4 CSVL（Central Sacral Vertical Line，后续 AVT/TS 会用）

CSVL 是穿过骶骨中心的垂直线。

**骶骨中心 x 坐标**：

\[
x_{CSVL} = \frac{SR.x + SL.x}{2}
\]

**CSVL 直线表达式**：

- `x = x_CSVL`

---

### 3.5 Trunk Shift（TS）

**常见定义**：C7 垂线（C7PL）与 CSVL 的水平距离。

#### 3.5.1 C7PL（C7 Plumb Line）

取 C7 椎体中心 x：

\[
x_{C7PL} = C7.center.x
\]

垂线：`x = x_C7PL`

#### 3.5.2 TS 计算

\[
TS = x_{C7PL} - x_{CSVL}
\]

若只关心幅度：

\[
|TS|
\]

对应 `measurements.type = "TS"`。你给的 TS points 在测量 JSON 中是两点同 y 的水平线段，等价于取两个垂线的水平差。

---

### 3.6 AVT（Apical Vertebral Translation）

**定义**：顶椎（apex）中心到 CSVL 的水平距离。

#### 3.6.1 选择顶椎 apex

对参与侧弯分析的椎体集合 `V`（例如胸段或全段），计算每节椎体中心到 CSVL 的距离：

\[
d_v = center(v).x - x_{CSVL}
\]

顶椎为使 `|d_v|` 最大的椎体：

\[
apex = \arg\max_{v \in V} |d_v|
\]

#### 3.6.2 AVT 计算

\[
AVT = |center(apex).x - x_{CSVL}|
\]

对应 `measurements.type = "AVT"`。你给的 AVT points 是同 y 的水平段，表达的就是“中心点到垂线”的水平距离。

---

### 3.7 Cobb Angle（CA）

**定义**：上终椎与下终椎终板线的夹角（取锐角）。

这里分两步：算每节椎体终板角度，然后选终椎，再算夹角。

#### 3.7.1 计算每节椎体终板角度

对椎体 `v`：

- 上终板角：
  \[
  \theta^{top}_v = angle(v.top\_left, v.top\_right)
  \]
- 下终板角：
  \[
  \theta^{bot}_v = angle(v.bottom\_left, v.bottom\_right)
  \]

#### 3.7.2 终椎选择（工程上常用两种策略）

**策略 A：全局最大 Cobb（最稳）**

枚举候选终椎组合（上终板 vs 下终板）取最大锐角：

\[
Cobb = \max_{i,j} acute\_angle(\theta_i, \theta_j)
\]

其中 `θ_i` 可取某椎体的上终板角，`θ_j` 取另一椎体的下终板角，并加入合理的“上在上方、下在下方”的约束（例如按 center.y 过滤）。

**策略 B：先定弯曲段再选终椎（更像临床）**

- 在某段（胸段/腰段）内找倾斜最大的上端终板作为上终椎
- 在同段内找倾斜方向相反且幅度最大的下端终板作为下终椎
- 取两者夹角

你当前测量 JSON 的 `CA.points` 是一条近乎水平的线段，`angle≈-0.75°`，更像“整体倾斜”或“示例输出”，不保证等同于临床严格 Cobb。若你要做标准 Cobb，推荐实现上述策略 A 或 B，而不是直接照抄 `CA.points`。

#### 3.7.3 Cobb 计算

选到上终板角 `θ_upper` 与下终板角 `θ_lower` 后：

\[
Cobb = acute\_angle(\theta_{upper}, \theta_{lower})
\]

对应 `measurements.type = "CA"`。

---

## 4. 与你两份 JSON 的对齐说明

你给的测量结果 `measurements` 中：

- `T1 Tilt.points` 正好对应 `vertebrae["T1"].corners.top_left/top_right`
- `Pelvic.points` 正好对应 `pose_keypoints.IR/IL`
- `Sacral.points` 正好对应 `pose_keypoints.SR/SL`
- `AVT.points`、`TS.points` 是“画水平段用”的点对，本质是 `x` 差值
- `CA.angle` 在你示例中接近 0，更像“某条参考线的倾斜”，不一定等同严格 Cobb

因此：计算时建议以 `pose_keypoints` 与 `vertebrae` 为主，把 `measurements` 当作可视化复核或调试输出。

---

## 5. 输出建议格式（与你的 `measurements` 一致）

建议统一输出结构：

- `type`: 指标名
- `points`: 用于可视化的关键点（如终板两点、水平线两端点）
- `angle`: 角度类填度数，平移类可填 `null` 或单独字段 `distance_px`

示例（Trunk Shift）：

- `points`: `(x_CSVL, y_ref)` 与 `(x_C7PL, y_ref)`
- `distance_px`: `abs(x_C7PL - x_CSVL)`

---

## 6. 小结

- 角度类（T1/Pelvic/Sacral/Cobb）都是：两点成线 + `atan2`
- 平移类（AVT/TS）都是：点的 `x` 与某条垂直中线 `x0` 的差
- Cobb 的难点是终椎选择规则，建议先做“全局最大 Cobb”保证稳定性，再按胸段/腰段做分段版本