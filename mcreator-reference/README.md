# ไฟล์ต้นฉบับจาก MCreator

โฟลเดอร์นี้เก็บโค้ด รูป และไฟล์ออกแบบของมอดต้นฉบับ `smeltingandforging` เพื่อใช้อ้างอิงสีและลายของชิ้นส่วน

- Java ในโฟลเดอร์นี้ไม่ได้รวมใน source set ของมอดปัจจุบัน โค้ดที่ต้องแก้เพื่อเปลี่ยนการทำงานอยู่ใน `../src/main/java/`
- `build.gradle` ยังนำ `src/main/resources/assets/smeltingandforging/textures/item/**` จากโฟลเดอร์นี้ไปบรรจุใน JAR เพราะ model ของชิ้นส่วนและพิมพ์เขียวปัจจุบันยังเรียกใช้รูปเหล่านี้
- `../scripts/generate_finished_equipment.py` อ่านรูปหัวและด้ามบางส่วนจากตำแหน่งเดียวกัน เพื่อสร้างภาพแยกชั้นของเครื่องมือสำเร็จ
- อย่าย้ายหรือลบรูปต้นฉบับโดยไม่ตรวจ model, สคริปต์ และ `processResources` ใน `build.gradle` พร้อมกัน

ไฟล์ที่ใช้จริงของ namespace `examplemod` อยู่ใน `../src/main/resources/` ดู [คู่มือโครงสร้าง](../docs/project-structure.md) สำหรับตำแหน่งใหม่
