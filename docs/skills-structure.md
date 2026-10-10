# โครงสร้างโค้ดสกิลหลังแยกคลาส

ตำแหน่งทั้งหมดในเอกสารนี้อยู่ใต้ `src/main/java/com/example/examplemod/skill/`

## จุดเริ่มต้นของการทำงาน

| จุดเริ่มต้น | หน้าที่ |
| --- | --- |
| `ForgedActiveSkills.java` | รับคำสั่งเลือก/ใช้สกิลจาก network แล้วส่งไปยังคลาสของสกิลนั้น |
| `ForgedEffectEvents.java` | ลงทะเบียน event กับ NeoForge แล้วส่งเหตุการณ์ไปยังตัวจัดการที่เกี่ยวข้อง |
| `ForgedEffectNetwork.java` | รับ payload จาก client และส่งข้อมูล HUD กลับ |
| `ForgedEffectRuntime.java` | อ่าน Tier ของ effect จากเครื่องมือ |
| `ForgedSkillConfig.java` | ค่าคูลดาวน์และค่าที่ผู้ใช้ปรับได้ |

`ForgedEffectEvents` เป็นจุดลงทะเบียนของ event ชุดเดิมเพียงจุดเดียว คลาสที่ลงท้ายด้วย `Handlers` และ `PlayerEffectTicker` ไม่ลงทะเบียน event ซ้ำ: ทุกเหตุการณ์เข้าผ่านเมธอดเดิมก่อนเรียกตัวจัดการย่อยหนึ่งครั้ง

## สกิลกดใช้

| คลาส | สกิลหรือหน้าที่ |
| --- | --- |
| `combat/ActiveCombatSkills.java` | Fireball Shoot, Harpoon Pull, Air Slash Rupture, Lava Wave และ Divine Beacon Light |
| `combat/ActiveCrowdControlSkills.java` | Stun Time Stop และ Gravitational Slam รวมถึงค้นหาเครื่องมือและปล่อยสกิลหลังชาร์จ |
| `combat/ActiveDefenseSkills.java` | Iron Fortress Guard, เปิด/ปิด Aegis Shield และหักความทนทานระหว่างเปิดโล่ |
| `movement/ActiveMovementSkills.java` | Front Dash และ Mob Swap |
| `mining/ActiveMiningSkills.java` | Ultimate Laser Breaker, รูปแบบขุดหลายบล็อก, Linear Penetration, Magnetic Clumping และ Obsidian Breaker |
| `farming/ActiveFarmingSkills.java` | Nature God Bless เมื่อกดใช้ |
| `building/ActiveBuildingSkills.java` | Block Levitation, Line Builder, Earthy Wall Rise และเปิด/ปิด Sky Bridge Walk |
| `storage/ForgedStorageSkills.java` | เปิด Pocket Dimension / Internal Storage และคัดลอกข้อมูลเก็บของเมื่อสร้าง player ใหม่ |

เมื่อเพิ่มสกิลใหม่ ให้เพิ่มรายการใน `ForgingEffect` และกำหนดการสุ่มใน `EffectPool` ตามระบบเดิม แล้วเพิ่มการเรียกใช้ใน `ForgedActiveSkills.use()` และรายการสกิลที่กดใช้ได้ใน `active/ActiveSkillState.isActive()`

## เอฟเฟคตามเหตุการณ์

| คลาส | เหตุการณ์หรือหน้าที่ |
| --- | --- |
| `combat/CombatEffectHandlers.java` | เมื่อได้รับ/สร้างความเสียหาย และเมื่อมอนสเตอร์ตาย เช่น Crippling Strike, Wither Drain และ Zombie Minion Calling |
| `mining/MiningEffectHandlers.java` | ความเร็วขุด ของดรอป การคลิกบล็อก และการทำลายบล็อก รวมถึงเอฟเฟคเก็บเกี่ยวที่เกิดขณะทำลายพืช |
| `farming/FarmingEffectHandlers.java` | คลิกขวาไถดิน การเติบโตพืช พื้นที่เร่งปลูก อนุภาค การเหยียบดิน และการวางบล็อก |
| `passive/PlayerEffectTicker.java` | เอฟเฟคขณะถือเครื่องมือ การซ่อม การติดตาม/ป้องกันของซอมบี้ และอัปเดตสกิลต่อเนื่องทุก tick |
| `storage/ForgedStorageSkills.java` | เหตุการณ์ clone ของผู้เล่นเพื่อรักษาของในช่องเก็บ |

ตัวจัดการบล็อกยังเก็บเอฟเฟคเก็บเกี่ยวที่อยู่ในเหตุการณ์ทำลายบล็อกไว้ด้วยกัน เพื่อให้ลำดับตรวจเงื่อนไขและการ `return` ตรงกับเดิม ส่วนระบบปลูกและเติบโตพืชอยู่ใน `FarmingEffectHandlers`

## ข้อมูลและตัวช่วยที่ใช้ร่วมกัน

| คลาส | ข้อมูลหรือหน้าที่ |
| --- | --- |
| `active/ActiveSkillState.java` | ลำดับสกิลที่เลือก คูลดาวน์บนไอเท็ม และการส่ง HUD |
| `active/ActiveSkillSupport.java` | ตรวจเป้าหมายที่เล็ง ลดความทนทาน และอ่านค่าตาม Tier |
| `active/ActiveWorldEffects.java` | อัปเดตลำแสง การชาร์จแรงโน้มถ่วง การหยุดเวลา สะพานชั่วคราว และบัฟพืชต่อเนื่อง |
| `passive/PassiveEffectSupport.java` | ตัวจับเวลาของเอฟเฟคตามเหตุการณ์และอ่านค่าตาม Tier |

คูลดาวน์สกิลกดใช้เก็บใน custom data ของไอเท็มด้วยคีย์ `forgedCooldown_...` ส่วนเอฟเฟคตามเหตุการณ์บางชนิดเก็บเวลาบนผู้เล่นด้วยคีย์ `ForgingCooldown_...` ทั้งสองมีขอบเขตข้อมูลต่างกัน จึงแยกตัวช่วยไว้ตามระบบเดิม

ค่าโอกาสและค่าคงที่แต่ละ Tier อยู่เป็น `private static final` ในคลาสที่ใช้ค่าเหล่านั้น ไม่กระจายสำเนาค่าเดียวกันหลายไฟล์ และไม่เปิด array เหล่านี้เป็น public

## ตัวอย่างตำแหน่งที่ต้องแก้

| ต้องการแก้ | ตำแหน่ง |
| --- | --- |
| ความแรง/ทิศทางของ Front Dash | `movement/ActiveMovementSkills.frontDash()` |
| รูปแบบเลเซอร์ขุดและการตรวจบล็อก | `mining/ActiveMiningSkills.ultimateLaser()` |
| โล่ Aegis ลดความเสียหาย | `combat/CombatEffectHandlers.onLivingAttack()` |
| โล่ Aegis ใช้ความทนทาน | `combat/ActiveDefenseSkills.tickAegis()` |
| ระยะ/ความเร็วการชาร์จ Gravitational Slam | `combat/ActiveCrowdControlSkills` และ `active/ActiveWorldEffects` |
| Auto-Smelt และของดรอป | `mining/MiningEffectHandlers.onBlockDrops()` |
| การเก็บเกี่ยวและ Frenzy Digging | `mining/MiningEffectHandlers.onBlockBreak()` และ `onBreakSpeed()` |
| Hyper Growth หรือความชื้นในแปลง | `farming/FarmingEffectHandlers` และข้อมูลแปลงใน `ForgedFarmingPlotData` |
| เปลี่ยนสกิลที่เลือกหรือแสดงคูลดาวน์บน HUD | `active/ActiveSkillState` และ `../client/skill/` |

การอ้างอิงเมธอดระหว่างคลาสใช้ static import ที่ระบุชื่อเมธอดชัดเจน ตัวช่วยที่จำเป็นข้ามหมวดเป็น public ส่วนเมธอดที่ใช้ภายในคลาสเดียวคงเป็น private

## แนวทางตรวจหลังแก้ไข

Build ด้วย Java 21 ตาม [คู่มือหลัก](project-structure.md) แล้วทดลองปุ่มเลือก/ใช้สกิล คูลดาวน์ การโจมตี ขุดแร่ ปลูกพืช สร้างบล็อก และช่องเก็บของทั้งในโลกผู้เล่นคนเดียวและเซิร์ฟเวอร์

รอบแยกคลาสนี้ย้ายเมธอดเดิมทั้งเมธอด คง API ของ `ForgedActiveSkills`, ชื่อและลำดับ event methods, คีย์ NBT, ค่า Tier, ลำดับสุ่ม และเงื่อนไขการลดความทนทาน การเปลี่ยนสมดุลสกิลให้ทำเป็นงานแยกจากการจัดโครงสร้าง
