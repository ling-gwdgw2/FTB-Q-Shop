# ling_q_shop (LING Quest Shop Mod)

ม็อดระบบร้านค้าและเศรษฐกิจ **Primogem Economy System** สำหรับ Minecraft **NeoForge 1.21.1** ออกแบบพิเศษสำหรับ Modpack แนวบุกเบิกอุตสาหกรรมเรือลอยฟ้า (Airship Pioneer Industry) โดย **LingRube**

---

## ฟีเจอร์เด่นของม็อด (Key Features)

### 1. หน้าต่างร้านค้า Custom Shop GUI
* **Sidebar Category Tabs**: แถบหมวดหมู่สินค้าด้านซ้าย จัดกลุ่มสินค้าอย่างเป็นระเบียบ สลับหมวดหมู่ลื่นไหล
* **Real-time Search Bar**: ช่องค้นหาไอเทม `Search items...` พิมพ์ชื่อไอเทมหรือไอดีม็อดเพื่อกรองไอเทมได้ทันที
* **Clean UI**: ดีไซน์เรียบหรู ดูง่าย ไม่บดบังสายตา

### 2. ระบบเศรษฐกิจ Primogem & ระบบขายของ (Sell System)
* **Primogem Crystal**: ใช้ Primogem เป็นสกุลเงินหลักในเกม (คลิกขวาเพื่อฝากเข้าคลังทีม)
* **Sell System (แลกเปลี่ยนแร่)**: ขายแร่ดิบหรือไอเทมส่วนเกินเพื่อแลกรับเหรียญ Primogems เข้าสู่บัญชีทีม

### 3. สะดวกสบายด้วย 3 ช่องทางเข้าถึงร้านค้า
*  **Auto Inventory Button**: ปุ่ม `Shop` อัตโนมัติในหน้าต่างกระเป๋าเก็บของ (เปิดกระเป๋า `E` แล้วกดปุ่มมุมขวาบนได้ทันที)
*  **Hotkey Keybinding**: กดปุ่ม **`B`** บนคีย์บอร์ดเพื่อเปิดร้านค้าทันที (ตั้งค่าเปลี่ยนปุ่มได้ในเมนู Controls)
*  **FTB Quests Integration**: ปุ่มไอคอนเหรียญ Primogem บนแถบเมนูด้านบนของหนังสือเควสต์ FTB Quests


## คำสั่งผู้ใช้งานและแอดมิน (Commands)

| คำสั่ง (Command) | สิทธิ์ (Permission) | คำอธิบาย (Description) |
| :--- | :--- | :--- |
| `/shop` หรือ `/hqs shop` | ผู้เล่นทุกคน | เปิดหน้าต่างร้านค้า ling_q_shop |
| `/hqs balance` | ผู้เล่นทุกคน | เช็กยอดเหรียญ Primogem ของตนเอง/ทีม |
| `/hqs addcoins <player> <amount>` | Admin (OP level 2) | เสกเพิ่มเหรียญ Primogem ให้ผู้เล่น |
| `/hqs removecoins <player> <amount>` | Admin (OP level 2) | หักเหรียญ Primogem จากผู้เล่น |
| `/hqs setcoins <player> <amount>` | Admin (OP level 2) | ตั้งค่ายอดเหรียญ Primogem ของผู้เล่น |

---

## วิธีการปรับแต่งสินค้าผ่าน Datapack (Customization)

ม็อด ling_q_shop รองรับการเพิ่ม/สามารถแก้ไข Item ได้ 3 วิธี:1เปลี่ยนเป็นเกมโหมดแล้วในร้านค้าจะมีเมนูเพิ่มไอเทมได้ตามที่ต้องการ 2 ระบบ Config 3 Data Packโดยตรง

### โครงสร้างโฟลเดอร์ Datapack:
```text
data/
└── questshop/
    └── questshop/
        ├── shop_categories/     <-- โฟลเดอร์เก็บไฟล์หมวดหมู่ JSON
        │   ├── create_tech.json
        │   └── sell.json
        └── shop_entries/        <-- โฟลเดอร์เก็บไฟล์รายการสินค้า JSON
            ├── create_tech.json
            └── sell_items.json
```

### ตัวอย่างไฟล์หมวดหมู่ (`shop_categories/example.json`):
```json
{
  "display": "Create Tech",
  "unlocked_by_default": true,
  "order": 1
}
```

### ตัวอย่างไฟล์รายการสินค้า (`shop_entries/example.json`):
```json
[
  {
    "item": "create:cogwheel",
    "amount": 8,
    "cost": 4,
    "category": "questshop:create_tech"
  }
]
```

---

### ข้อมูลโปรเจกต์ (Project Info)

* **Mod Name**: LINGQshop
* **Author**: LingRube
* **Version**: 1.0
* **Target Minecraft**: NeoForge 1.21.1
* **Website**: [Garden of Dreams](https://garden-of-dreams-4768a.firebaseapp.com/)
* **Source & Issues**: [GitHub Repository](https://github.com/ling-gwdgw2/FTB-Q-Shop.git)
