# ling_q_shop (LING Quest Shop Mod)

An in-game shop and economy system (**Primogem Economy System**) for **Minecraft NeoForge 1.21.1**. Specially designed for modpacks and industrial adventures by **LingRube**.

---

## 🌟 Key Features

### 1. Custom Shop GUI
* **Sidebar Category Tabs**: Clean vertical sidebar for organizing item categories with smooth selection highlights (`> Active`).
* **Mouse Wheel Scroll**: Seamlessly scroll through category lists using your mouse wheel if there are many categories.
* **Real-time Search Bar**: Filter items instantly by item name, ID, or mod namespace (`Search items...`).
* **Visual Price Display**: Displays real item quantity and cost accompanied by the official **Primogem Coin** icon (`coin.png`).
* **Hover Tooltips**: Hover over any shop item to inspect its full item details, enchantments, and lore before purchasing.

### 2. In-GUI Admin & Creative Mode Tools
When in Creative Mode (`/gamemode creative`), admins can edit the shop directly inside the GUI:
* **`+ Browse Items` Modal**: Search and add any registered Minecraft item to the shop with custom prices and pagination (supports Mouse Wheel scroll).
* **`+ Hand`**: Instantly add the item currently held in your main hand to the shop.
* **Edit & Remove**: Adjust item prices, item amounts, or delete entries directly from the shop interface.
* **Category Manager (`+ Cat` / `E`)**: Create, edit, and delete categories directly inside the game.

### 3. Convenient Access Methods
*  **Inventory Button**: Automatic `Shop` button integrated into the player inventory screen (`E`).
*  **Keybinding**: Press **`B`** (configurable) to open the shop instantly.
*  **FTB Quests Integration**: Compatible with FTB Quests & FTB Teams for team economy and quest reward unlocking.

---

## 📜 Commands

| Command | Permission | Description |
| :--- | :--- | :--- |
| `/shop` or `/hqs shop` | All Players | Opens the `ling_q_shop` GUI |
| `/hqs balance` | All Players | Checks your current Primogem coin balance |
| `/hqs addcoins <player> <amount>` | Admin (OP Level 2) | Adds Primogem coins to a player/team balance |
| `/hqs removecoins <player> <amount>` | Admin (OP Level 2) | Removes Primogem coins from a player/team balance |
| `/hqs setcoins <player> <amount>` | Admin (OP Level 2) | Sets the exact Primogem coin balance for a player/team |

---

## ⚙️ Customization & Datapacks

You can configure and customize shop entries and categories in 3 easy ways:
1. **In-Game Creative Mode UI**: Toggle `[Edit: ON]` inside the shop GUI to add/modify items and categories directly.
2. **Config Directory**: Files located in `config/ling_q_shop/shop_categories/*.json` and `config/ling_q_shop/shop_entries/*.json`.
3. **Datapack Support**: Override or add items via Datapacks.

### Datapack Directory Structure:
```text
data/
└── ling_q_shop/
    ├── shop_categories/     <-- Category JSON files
    │   ├── create_tech.json
    │   └── sell.json
    └── shop_entries/        <-- Item entry JSON files
        ├── create_tech.json
        └── sell_items.json
```

### Example Category File (`shop_categories/example.json`):
```json
{
  "display": "Create Tech",
  "unlocked_by_default": true,
  "order": 1
}
```

### Example Shop Entry File (`shop_entries/example.json`):
```json
[
  {
    "item": "create:cogwheel",
    "amount": 8,
    "cost": 4,
    "category": "ling_q_shop:create_tech"
  }
]
```

---

## 📋 Project Information

* **Mod Name**: LING Quest Shop (`ling_q_shop`)
* **Author**: LingRube
* **Version**: 1.2
* **Target Minecraft**: NeoForge 1.21.1 (NeoForge 21.1.208+)
* **Website**: [Garden of Dreams](https://garden-of-dreams-4768a.firebaseapp.com/)
* **GitHub Repository**: [ling-gwdgw2/FTB-Q-Shop](https://github.com/ling-gwdgw2/FTB-Q-Shop)
