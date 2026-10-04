# โครงสร้างโปรเจกต์ Smelting & Forging

มอดนี้ใช้ Minecraft 1.21.1, NeoForge 21.1.244 และ Java 21 โค้ดที่ทำงานจริงอยู่ใน `src/main/java/` และทรัพยากรหลักอยู่ใน `src/main/resources/`

## โฟลเดอร์ระดับโปรเจกต์

| ตำแหน่ง | หน้าที่ |
| --- | --- |
| `src/main/java/com/example/examplemod/` | โค้ดมอดปัจจุบัน |
| `src/main/resources/assets/examplemod/` | Model, texture, blockstate และข้อความภาษา |
| `src/main/resources/data/` | สูตรคราฟต์ advancement, loot table และ tag |
| `src/main/templates/META-INF/neoforge.mods.toml` | ต้นแบบข้อมูลมอด ใช้ค่าจาก `gradle.properties` |
| `mcreator-reference/` | โค้ดและรูปต้นฉบับ MCreator; รูป item บางส่วนยังบรรจุใน JAR |
| `scripts/` | สคริปต์สร้างภาพและ model |
| `docs/` | คู่มือสำหรับพัฒนาและอธิบายโครงงาน |
| `build.gradle`, `gradle.properties`, `settings.gradle` | ตั้งค่า build, dependency และเวอร์ชัน |
| `gradle/wrapper/`, `gradlew`, `gradlew.bat` | Gradle Wrapper สำหรับ build |
| `.github/workflows/build.yml` | ตรวจ build บน GitHub ด้วย Java 21 |

`build/` และ `run/` เป็นผลลัพธ์หรือข้อมูลระหว่างพัฒนา ไม่ใช่ไฟล์ต้นฉบับที่ต้องแก้

## หมวด Java

ทุกตำแหน่งในตารางต่อไปนี้อยู่ใต้ `src/main/java/com/example/examplemod/`

| หมวด | ตัวอย่างและหน้าที่ |
| --- | --- |
| ระดับหลัก | `ExampleMod.java` ลงทะเบียน block, item, menu, entity และ effect; `Config.java` เป็น config จาก template |
| `block/` | เตาหลอม ทั่ง บล็อกรองรับ และดินเก็บความชื้น |
| `item/` | ข้อมูลและพฤติกรรมของหัว แก่น ด้าม และเครื่องมือสำเร็จ |
| `entity/` | `ForgedBoomerangEntity.java` ควบคุมบูมเมอแรงในโลก |
| `menu/` | `ForgeMenu`, `AnvilMenu`, `ForgedStorageMenu`: ช่อง inventory และการทำงานที่ฝั่งเซิร์ฟเวอร์ตรวจสอบ |
| `forging/material/` | `ForgingMetal`, `MonsterMaterial`: ประเภทโลหะและวัตถุดิบมอนสเตอร์ |
| `forging/blueprint/` | `ForgingBlueprintType`, `HeadBlueprintType`: ประเภทชิ้นส่วนและเครื่องมือ |
| `forging/ingredient/` | `ForgeIngredientResolver`: ตรวจพิมพ์เขียว วัตถุดิบ จำนวนโลหะ และเชื้อเพลิง |
| `forging/result/` | Record เก็บผลตีชิ้นส่วน ผลมินิเกม และ `AnvilAssemblyResult` สำหรับการประกอบ |
| `forging/session/` | `ForgeRewardSession`, `AnvilRewardSession`: ตรวจสิทธิ์รับผลลัพธ์จากการตี |
| `client/` | `ExampleModClient` และ hook ฝั่ง client |
| `client/screen/` | GUI เตาหลอม ทั่ง ช่องเก็บของ และหน้าผลลัพธ์ |
| `client/minigame/` | Timing Bar สำหรับหัว/แก่น/ด้าม และ Rhythm สำหรับการประกอบ |
| `client/ui/` | `ForgingMinigameArt`, `ForgingResultArt`, `MinigameFeedback`: ภาพประกอบและเสียง UI |
| `client/render/` | `ForgedBoomerangRenderer`: วาดบูมเมอแรง |
| `client/guide/` | หน้าจอหนังสือและข้อมูลการค้นพบที่ส่งมาให้ client |
| `client/skill/` | ปุ่มสกิลและ HUD |
| `guide/` | ไอเท็มหนังสือ ข้อความอธิบาย การแจกหนังสือ และสมุดบันทึกการค้นพบฝั่งเซิร์ฟเวอร์ |
| `skill/` | รายการ effect, Tier, config, network และพฤติกรรมสกิล |
| `skill/blessing/` | พรและเงื่อนไขการทำงาน |
| `skill/curse/` | คำสาปและบทพูดของ Talkative Blade |
| `debug/block/`, `debug/menu/`, `debug/client/` | บล็อก เมนู และหน้าจอสำหรับทดสอบอุปกรณ์ พร และคำสาป |

การย้าย Java ต้องแก้ `package` ในไฟล์ที่ย้ายและ `import` ในไฟล์ที่เรียกใช้ด้วย ชื่อคลาสเดิมยังใช้ต่อ เช่น `ForgeMenu` เปลี่ยนตำแหน่งเป็น `com.example.examplemod.menu.ForgeMenu`

## ถ้าต้องการแก้ระบบนี้ ให้เปิดไฟล์ไหน

| งาน | ไฟล์หลัก |
| --- | --- |
| ตำแหน่งช่อง/หน้าจอเตาหลอม | `menu/ForgeMenu.java`, `client/screen/ForgingScreen.java` |
| เชื้อเพลิงและเงื่อนไขวัตถุดิบ | `forging/ingredient/ForgeIngredientResolver.java`, `menu/ForgeMenu.java`, `block/ForgingBlockEntity.java` |
| หน้าจอทั่งและการประกอบ | `menu/AnvilMenu.java`, `client/screen/ForgingAnvilScreen.java`, `forging/result/AnvilAssemblyResult.java` |
| Timing Bar | `client/minigame/TimingBarScreen.java`, `CoreTimingBarScreen.java`, `RodTimingBarScreen.java` |
| Rhythm ของทั่ง | `client/minigame/AnvilRhythmForgingScreen.java` |
| หน้าผลลัพธ์การตี | `client/screen/*ForgingResultScreen.java`, `ForgingResultScreen.java`, `client/ui/ForgingResultArt.java` |
| Tooltip และข้อมูลเครื่องมือ | `item/ForgedEquipmentItem.java` และไฟล์ไอเท็มชิ้นส่วนใน `item/` |
| เพิ่มหรือแก้รายการสกิล | `skill/ForgingEffect.java`, `skill/EffectPool.java` |
| สกิลกด R | `client/skill/ForgedEffectKeybinds.java`, `skill/ForgedEffectNetwork.java`, `skill/ForgedActiveSkills.java` |
| เอฟเฟคตามเหตุการณ์ | `skill/ForgedEffectEvents.java` |
| คูลดาวน์และค่าปรับแต่ง | `skill/ForgedSkillConfig.java`; ตรวจข้อความใน `guide/ForgingGuideDescriptions.java` ให้ตรงกัน |
| พร/คำสาป | `skill/blessing/`, `skill/curse/` |
| ดาบพูดมาก | `skill/curse/TalkativeBladeEvents.java`, `TalkativeBladeDialogue.java` |
| หนังสือคู่มือ | `client/guide/ForgingGuideScreen.java`, `guide/ForgingGuideDescriptions.java`, `ForgingGuideJournal.java` |
| แจกหนังสือเริ่มเกม | `guide/ForgingGuideStarterEvents.java` |
| ลงทะเบียนไอเท็ม/บล็อกใหม่ | `ExampleMod.java` แล้วเพิ่ม model, ภาษา และสูตรใน resources |

## ทรัพยากรและรูปภาพ

| ตำแหน่งใต้ `src/main/resources/assets/examplemod/` | เนื้อหา |
| --- | --- |
| `textures/block/` | รูปเตาหลอมและทั่ง |
| `textures/item/parts/head/` | รูปต้นฉบับหัวเพิ่มเติมของ namespace `examplemod` |
| `textures/item/parts/core/` | รูปแก่นเพิ่มเติม |
| `textures/item/parts/rod/` | รูปด้ามเพิ่มเติม |
| `textures/item/equipment/head/` | รูปหัวสำหรับเครื่องมือสำเร็จ |
| `textures/item/equipment/rod/` | รูปด้ามสำหรับเครื่องมือสำเร็จ |
| `textures/item/equipment/default/` | รูปเครื่องมือสำรอง |
| `models/item/` | Model ของไอเท็ม รวมถึงตัวเลือกหัวและด้าม |
| `models/block/`, `blockstates/` | รูปทรงและสถานะของบล็อก |
| `lang/` | ข้อความภาษา |

ตัวอย่าง texture ใน JSON: `examplemod:item/equipment/head/finished_sword_head_flesh` หมายถึงไฟล์ `textures/item/equipment/head/finished_sword_head_flesh.png` ใน namespace `examplemod`

โฟลเดอร์ `models/item/` ยังรักษาชื่อ model เดิมไว้ เพราะ override อ้างอิงชื่อเหล่านี้ ส่วน `data/examplemod/recipe/`, `advancement/`, `loot_table/` และ `data/minecraft/tags/block/` คงรูปแบบและรหัสเดิมเพื่อรักษาสูตรและการอ้างอิง

ไม่มีภาพ GUI PNG ใน namespace นี้ ณ รอบจัดโครงสร้างนี้: หน้าจอส่วนใหญ่ใช้การวาดด้วย Java ใน `client/screen/` และ `client/ui/` หากเพิ่มภาพ GUI ภายหลังให้ใช้ `textures/gui/` และระบุเส้นทางใน Java ที่เรียกใช้

รูปใน namespace `smeltingandforging` ยังมาจาก `mcreator-reference/src/main/resources/assets/smeltingandforging/textures/item/` ดู [ข้อกำหนดไฟล์อ้างอิง](../mcreator-reference/README.md) ก่อนแก้

## สคริปต์สร้างทรัพยากร

- `python scripts/generate_finished_equipment.py` อ่านรูปหัว/ด้ามต้นฉบับ สร้าง 65 รูปหัว, 13 รูปด้าม และ 845 model ของเครื่องมือสำเร็จตามลำดับ enum เดิม ผลลัพธ์รูปอยู่ใน `textures/item/equipment/`
- `python scripts/generate_forging_block_assets.py` สร้างรูปบล็อกเตาหลอมและทั่ง

เรียกสคริปต์แรกได้จากทุกโฟลเดอร์โดยใช้เส้นทางของสคริปต์ ส่วนสคริปต์รูปบล็อกต้องมี Pillow ติดตั้งก่อน ตรวจ diff หลังสร้างภาพทุกครั้ง เพราะสคริปต์จะเขียนทับผลลัพธ์ที่สร้างได้

## Build และตรวจในเกม

บน Windows PowerShell ใช้ Java 21 และสั่งจากโฟลเดอร์โปรเจกต์:

```powershell
./gradlew.bat build
./gradlew.bat runClient
```

บน Linux/macOS ใช้ `./gradlew build` และ `./gradlew runClient` ผลลัพธ์ JAR อยู่ใน `build/libs/`

หลังย้ายไฟล์ให้ตรวจ build และทดลองรายการต่อไปนี้:

1. เตาหลอม: ช่องวัตถุดิบ เชื้อเพลิง และมินิเกมหัว/แก่น/ด้าม
2. ทั่ง: ประกอบครบสามชิ้น มินิเกม Rhythm และรับเครื่องมือ
3. Model และ texture: หัว/ด้ามหลายวัสดุ รูปพิมพ์เขียว และบล็อก
4. หนังสือ: เปิด อ่านการค้นพบ และปิดหน้าจอ
5. ปุ่ม R, HUD, ช่องเก็บของ และบูมเมอแรง
6. บล็อกทดสอบอุปกรณ์ พร และคำสาป
7. เปิดเซิร์ฟเวอร์และเชื่อมต่อ client เพื่อตรวจการลงทะเบียนเมนูและ network

## ขอบเขตรอบจัดโครงสร้าง

รอบนี้ย้ายตำแหน่งคลาส จัดหมวด texture และแก้เส้นทางอ้างอิง โดยคง Mod ID, รหัสไอเท็ม/บล็อก/menu/entity, ลำดับ enum, NBT, payload และชื่อ model เดิม ข้อมูลเหล่านี้สัมพันธ์กับเซฟและการทำงานของมอด

การแยกเนื้อหาภายใน `ForgedActiveSkills.java` และ `ForgedEffectEvents.java` เป็นหมวด mining/combat/farming/movement เป็นงานรอบถัดไป ต้องตรวจข้อมูลร่วมและเงื่อนไขแต่ละสกิลก่อนแยก
