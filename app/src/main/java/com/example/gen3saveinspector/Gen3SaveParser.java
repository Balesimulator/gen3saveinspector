package com.example.gen3saveinspector;

import java.util.*;
import java.nio.charset.StandardCharsets;

public final class Gen3SaveParser {
    private Gen3SaveParser() {}

    public static final int SAVE_SIZE = 0x20000;
    private static final int SECTION_SIZE = 0x1000;
    private static final long SIGNATURE = 0x08012025L;

    private static final int[] SECTION_DATA_SIZES = {
        3884,3968,3968,3968,3848,3968,3968,3968,3968,3968,3968,3968,3968,2000
    };

    private static final String[] SUBSTRUCT_ORDERS = {
        "GAEM","GAME","GEAM","GEMA","GMAE","GMEA",
        "AGEM","AGME","AEGM","AEMG","AMGE","AMEG",
        "EGAM","EGMA","EAGM","EAMG","EMGA","EMAG",
        "MGAE","MGEA","MAGE","MAEG","MEGA","MEAG"
    };

    public static final String[] NATURES = {
        "勤奋/がんばりや\t--------",
        "怕寂寞/さみしがり\tAtt+Def-",
        "勇敢/ゆうかん\tAtt+Spe-",
        "固执/いじっぱり\tAtt+SpA-",
        "顽皮/やんちゃ\tAtt+SpD-",

        "大胆/ずぶとい\tDef+Att-",
        "坦率/すなお\t--------",
        "悠闲/のんき\tDef+Spe-",
        "淘气/わんぱく\tDef+SpA-",
        "乐天/のうてんき\tDef+SpD-",

        "胆小/おくびょう\tSpe+Att-",
        "急躁/せっかち\tSpe+Def-",
        "认真/まじめ\t--------",
        "爽朗/ようき\tSpe+SpA-",
        "天真/むじゃき\tSpe+SpD-",

        "内敛/ひかえめ\tSpA+Att-",
        "慢吞吞/おっとり\tSpA+Def-",
        "冷静/れいせい\tSpA+Spe-",
        "害羞/てれや\t--------",
        "马虎/うっかりや\tSpA+SpD-",

        "温和/おだやか\tSpD+Att-",
        "温顺/おとなしい\tSpD+Def-",
        "自大/なまいき\tSpD+Spe-",
        "慎重/しんちょう\tSpD+SpA-",
        "浮躁/きまぐれ\t--------"
};
/*
    private static final String[] SPECIES = {
        "NONE",
        "Bulbasaur",
        "Ivysaur",
        "Venusaur",
        "Charmander",
        "Charmeleon",
        "Charizard",
        "Squirtle",
        "Wartortle",
        "Blastoise",
        "Caterpie",
        "Metapod",
        "Butterfree",
        "Weedle",
        "Kakuna",
        "Beedrill",
        "Pidgey",
        "Pidgeotto",
        "Pidgeot",
        "Rattata",
        "Raticate",
        "Spearow",
        "Fearow",
        "Ekans",
        "Arbok",
        "Pikachu",
        "Raichu",
        "Sandshrew",
        "Sandslash",
        "Nidoran_F",
        "Nidorina",
        "Nidoqueen",
        "Nidoran_M",
        "Nidorino",
        "Nidoking",
        "Clefairy",
        "Clefable",
        "Vulpix",
        "Ninetales",
        "Jigglypuff",
        "Wigglytuff",
        "Zubat",
        "Golbat",
        "Oddish",
        "Gloom",
        "Vileplume",
        "Paras",
        "Parasect",
        "Venonat",
        "Venomoth",
        "Diglett",
        "Dugtrio",
        "Meowth",
        "Persian",
        "Psyduck",
        "Golduck",
        "Mankey",
        "Primeape",
        "Growlithe",
        "Arcanine",
        "Poliwag",
        "Poliwhirl",
        "Poliwrath",
        "Abra",
        "Kadabra",
        "Alakazam",
        "Machop",
        "Machoke",
        "Machamp",
        "Bellsprout",
        "Weepinbell",
        "Victreebel",
        "Tentacool",
        "Tentacruel",
        "Geodude",
        "Graveler",
        "Golem",
        "Ponyta",
        "Rapidash",
        "Slowpoke",
        "Slowbro",
        "Magnemite",
        "Magneton",
        "Farfetchd",
        "Doduo",
        "Dodrio",
        "Seel",
        "Dewgong",
        "Grimer",
        "Muk",
        "Shellder",
        "Cloyster",
        "Gastly",
        "Haunter",
        "Gengar",
        "Onix",
        "Drowzee",
        "Hypno",
        "Krabby",
        "Kingler",
        "Voltorb",
        "Electrode",
        "Exeggcute",
        "Exeggutor",
        "Cubone",
        "Marowak",
        "Hitmonlee",
        "Hitmonchan",
        "Lickitung",
        "Koffing",
        "Weezing",
        "Rhyhorn",
        "Rhydon",
        "Chansey",
        "Tangela",
        "Kangaskhan",
        "Horsea",
        "Seadra",
        "Goldeen",
        "Seaking",
        "Staryu",
        "Starmie",
        "Mr_Mime",
        "Scyther",
        "Jynx",
        "Electabuzz",
        "Magmar",
        "Pinsir",
        "Tauros",
        "Magikarp",
        "Gyarados",
        "Lapras",
        "Ditto",
        "Eevee",
        "Vaporeon",
        "Jolteon",
        "Flareon",
        "Porygon",
        "Omanyte",
        "Omastar",
        "Kabuto",
        "Kabutops",
        "Aerodactyl",
        "Snorlax",
        "Articuno",
        "Zapdos",
        "Moltres",
        "Dratini",
        "Dragonair",
        "Dragonite",
        "Mewtwo",
        "Mew",
        "Chikorita",
        "Bayleef",
        "Meganium",
        "Cyndaquil",
        "Quilava",
        "Typhlosion",
        "Totodile",
        "Croconaw",
        "Feraligatr",
        "Sentret",
        "Furret",
        "Hoothoot",
        "Noctowl",
        "Ledyba",
        "Ledian",
        "Spinarak",
        "Ariados",
        "Crobat",
        "Chinchou",
        "Lanturn",
        "Pichu",
        "Cleffa",
        "Igglybuff",
        "Togepi",
        "Togetic",
        "Natu",
        "Xatu",
        "Mareep",
        "Flaaffy",
        "Ampharos",
        "Bellossom",
        "Marill",
        "Azumarill",
        "Sudowoodo",
        "Politoed",
        "Hoppip",
        "Skiploom",
        "Jumpluff",
        "Aipom",
        "Sunkern",
        "Sunflora",
        "Yanma",
        "Wooper",
        "Quagsire",
        "Espeon",
        "Umbreon",
        "Murkrow",
        "Slowking",
        "Misdreavus",
        "Unown",
        "Wobbuffet",
        "Girafarig",
        "Pineco",
        "Forretress",
        "Dunsparce",
        "Gligar",
        "Steelix",
        "Snubbull",
        "Granbull",
        "Qwilfish",
        "Scizor",
        "Shuckle",
        "Heracross",
        "Sneasel",
        "Teddiursa",
        "Ursaring",
        "Slugma",
        "Magcargo",
        "Swinub",
        "Piloswine",
        "Corsola",
        "Remoraid",
        "Octillery",
        "Delibird",
        "Mantine",
        "Skarmory",
        "Houndour",
        "Houndoom",
        "Kingdra",
        "Phanpy",
        "Donphan",
        "Porygon2",
        "Stantler",
        "Smeargle",
        "Tyrogue",
        "Hitmontop",
        "Smoochum",
        "Elekid",
        "Magby",
        "Miltank",
        "Blissey",
        "Raikou",
        "Entei",
        "Suicune",
        "Larvitar",
        "Pupitar",
        "Tyranitar",
        "Lugia",
        "Ho_Oh",
        "Celebi",
        "OLD_UNOWN_B",
        "OLD_UNOWN_C",
        "OLD_UNOWN_D",
        "OLD_UNOWN_E",
        "OLD_UNOWN_F",
        "OLD_UNOWN_G",
        "OLD_UNOWN_H",
        "OLD_UNOWN_I",
        "OLD_UNOWN_J",
        "OLD_UNOWN_K",
        "OLD_UNOWN_L",
        "OLD_UNOWN_M",
        "OLD_UNOWN_N",
        "OLD_UNOWN_O",
        "OLD_UNOWN_P",
        "OLD_UNOWN_Q",
        "OLD_UNOWN_R",
        "OLD_UNOWN_S",
        "OLD_UNOWN_T",
        "OLD_UNOWN_U",
        "OLD_UNOWN_V",
        "OLD_UNOWN_W",
        "OLD_UNOWN_X",
        "OLD_UNOWN_Y",
        "OLD_UNOWN_Z",
        "Treecko",
        "Grovyle",
        "Sceptile",
        "Torchic",
        "Combusken",
        "Blaziken",
        "Mudkip",
        "Marshtomp",
        "Swampert",
        "Poochyena",
        "Mightyena",
        "Zigzagoon",
        "Linoone",
        "Wurmple",
        "Silcoon",
        "Beautifly",
        "Cascoon",
        "Dustox",
        "Lotad",
        "Lombre",
        "Ludicolo",
        "Seedot",
        "Nuzleaf",
        "Shiftry",
        "Nincada",
        "Ninjask",
        "Shedinja",
        "Taillow",
        "Swellow",
        "Shroomish",
        "Breloom",
        "Spinda",
        "Wingull",
        "Pelipper",
        "Surskit",
        "Masquerain",
        "Wailmer",
        "Wailord",
        "Skitty",
        "Delcatty",
        "Kecleon",
        "Baltoy",
        "Claydol",
        "Nosepass",
        "Torkoal",
        "Sableye",
        "Barboach",
        "Whiscash",
        "Luvdisc",
        "Corphish",
        "Crawdaunt",
        "Feebas",
        "Milotic",
        "Carvanha",
        "Sharpedo",
        "Trapinch",
        "Vibrava",
        "Flygon",
        "Makuhita",
        "Hariyama",
        "Electrike",
        "Manectric",
        "Numel",
        "Camerupt",
        "Spheal",
        "Sealeo",
        "Walrein",
        "Cacnea",
        "Cacturne",
        "Snorunt",
        "Glalie",
        "Lunatone",
        "Solrock",
        "Azurill",
        "Spoink",
        "Grumpig",
        "Plusle",
        "Minun",
        "Mawile",
        "Meditite",
        "Medicham",
        "Swablu",
        "Altaria",
        "Wynaut",
        "Duskull",
        "Dusclops",
        "Roselia",
        "Slakoth",
        "Vigoroth",
        "Slaking",
        "Gulpin",
        "Swalot",
        "Tropius",
        "Whismur",
        "Loudred",
        "Exploud",
        "Clamperl",
        "Huntail",
        "Gorebyss",
        "Absol",
        "Shuppet",
        "Banette",
        "Seviper",
        "Zangoose",
        "Relicanth",
        "Aron",
        "Lairon",
        "Aggron",
        "Castform",
        "Volbeat",
        "Illumise",
        "Lileep",
        "Cradily",
        "Anorith",
        "Armaldo",
        "Ralts",
        "Kirlia",
        "Gardevoir",
        "Bagon",
        "Shelgon",
        "Salamence",
        "Beldum",
        "Metang",
        "Metagross",
        "Regirock",
        "Regice",
        "Registeel",
        "Kyogre",
        "Groudon",
        "Rayquaza",
        "Latias",
        "Latios",
        "Jirachi",
        "Deoxys",
        "Chimecho",
        "EGG"
    };
    */
    private static final String[] SPECIES = {
        "NONE",
        "妙蛙种子/フシギダネ",
        "妙蛙草/フシギソウ",
        "妙蛙花/フシギバナ",
        "小火龙/ヒトカゲ",
        "火恐龙/リザード",
        "喷火龙/リザードン",
        "杰尼龟/ゼニガメ",
        "卡咪龟/カメール",
        "水箭龟/カメックス",
        "绿毛虫/キャタピー",
        "铁甲蛹/トランセル",
        "巴大蝶/バタフリー",
        "独角虫/ビードル",
        "铁壳蛹/コクーン",
        "大针蜂/スピアー",
        "波波/ポッポ",
        "比比鸟/ピジョン",
        "大比鸟/ピジョット",
        "小拉达/コラッタ",
        "拉达/ラッタ",
        "烈雀/オニスズメ",
        "大嘴雀/オニドリル",
        "阿柏蛇/アーボ",
        "阿柏怪/アーボック",
        "皮卡丘/ピカチュウ",
        "雷丘/ライチュウ",
        "穿山鼠/サンド",
        "穿山王/サンドパン",
        "尼多兰/ニドラン♀",
        "尼多娜/ニドリーナ",
        "尼多后/ニドクイン",
        "尼多朗/ニドラン♂",
        "尼多力诺/ニドリーノ",
        "尼多王/ニドキング",
        "皮皮/ピッピ",
        "皮可西/ピクシー",
        "六尾/ロコン",
        "九尾/キュウコン",
        "胖丁/プリン",
        "胖可丁/プクリン",
        "超音蝠/ズバット",
        "大嘴蝠/ゴルバット",
        "走路草/ナゾノクサ",
        "臭臭花/クサイハナ",
        "霸王花/ラフレシア",
        "派拉斯/パラス",
        "派拉斯特/パラセクト",
        "毛球/コンパン",
        "摩鲁蛾/モルフォン",
        "地鼠/ディグダ",
        "三地鼠/ダグトリオ",
        "喵喵/ニャース",
        "猫老大/ペルシアン",
        "可达鸭/コダック",
        "哥达鸭/ゴルダック",
        "猴怪/マンキー",
        "火暴猴/オコリザル",
        "卡蒂狗/ガーディ",
        "风速狗/ウインディ",
        "蚊香蝌蚪/ニョロモ",
        "蚊香君/ニョロゾ",
        "蚊香泳士/ニョロボン",
        "凯西/ケーシィ",
        "勇基拉/ユンゲラー",
        "胡地/フーディン",
        "腕力/ワンリキー",
        "豪力/ゴーリキー",
        "怪力/カイリキー",
        "喇叭芽/マダツボミ",
        "口呆花/ウツドン",
        "大食花/ウツボット",
        "玛瑙水母/メノクラゲ",
        "毒刺水母/ドククラゲ",
        "小拳石/イシツブテ",
        "隆隆石/ゴローン",
        "隆隆岩/ゴローニャ",
        "小火马/ポニータ",
        "烈焰马/ギャロップ",
        "呆呆兽/ヤドン",
        "呆壳兽/ヤドラン",
        "小磁怪/コイル",
        "三合一磁怪/レアコイル",
        "大葱鸭/カモネギ",
        "嘟嘟/ドードー",
        "嘟嘟利/ドードリオ",
        "小海狮/パウワウ",
        "白海狮/ジュゴン",
        "臭泥/ベトベター",
        "臭臭泥/ベトベトン",
        "大舌贝/シェルダー",
        "刺甲贝/パルシェン",
        "鬼斯/ゴース",
        "鬼斯通/ゴースト",
        "耿鬼/ゲンガー",
        "大岩蛇/イワーク",
        "催眠貘/スリープ",
        "引梦貘人/スリーパー",
        "大钳蟹/クラブ",
        "巨钳蟹/キングラー",
        "霹雳电球/ビリリダマ",
        "顽皮雷弹/マルマイン",
        "蛋蛋/タマタマ",
        "椰蛋树/ナッシー",
        "卡拉卡拉/カラカラ",
        "嘎啦嘎啦/ガラガラ",
        "飞腿郎/サワムラー",
        "快拳郎/エビワラー",
        "大舌头/ベロリンガ",
        "瓦斯弹/ドガース",
        "双弹瓦斯/マタドガス",
        "独角犀牛/サイホーン",
        "钻角犀兽/サイドン",
        "吉利蛋/ラッキー",
        "蔓藤怪/モンジャラ",
        "袋兽/ガルーラ",
        "墨海马/タッツー",
        "海刺龙/シードラ",
        "角金鱼/トサキント",
        "金鱼王/アズマオウ",
        "海星星/ヒトデマン",
        "宝石海星/スターミー",
        "魔墙人偶/バリヤード",
        "飞天螳螂/ストライク",
        "迷唇姐/ルージュラ",
        "电击兽/エレブー",
        "鸭嘴火兽/ブーバー",
        "凯罗斯/カイロス",
        "肯泰罗/ケンタロス",
        "鲤鱼王/コイキング",
        "暴鲤龙/ギャラドス",
        "拉普拉斯/ラプラス",
        "百变怪/メタモン",
        "伊布/イーブイ",
        "水伊布/シャワーズ",
        "雷伊布/サンダース",
        "火伊布/ブースター",
        "多边兽/ポリゴン",
        "菊石兽/オムナイト",
        "多刺菊石兽/オムスター",
        "化石盔/カブト",
        "镰刀盔/カブトプス",
        "化石翼龙/プテラ",
        "卡比兽/カビゴン",
        "急冻鸟/フリーザー",
        "闪电鸟/サンダー",
        "火焰鸟/ファイヤー",
        "迷你龙/ミニリュウ",
        "哈克龙/ハクリュー",
        "快龙/カイリュー",
        "超梦/ミュウツー",
        "梦幻/ミュウ",
        "菊草叶/チコリータ",
        "月桂叶/ベイリーフ",
        "大竺葵/メガニウム",
        "火球鼠/ヒノアラシ",
        "火岩鼠/マグマラシ",
        "火暴兽/バクフーン",
        "小锯鳄/ワニノコ",
        "蓝鳄/アリゲイツ",
        "大力鳄/オーダイル",
        "尾立/オタチ",
        "大尾立/オオタチ",
        "咕咕/ホーホー",
        "猫头夜鹰/ヨルノズク",
        "芭瓢虫/レディバ",
        "安瓢虫/レディアン",
        "圆丝蛛/イトマル",
        "阿利多斯/アリアドス",
        "叉字蝠/クロバット",
        "灯笼鱼/チョンチー",
        "电灯怪/ランターン",
        "皮丘/ピチュー",
        "皮宝宝/ピィ",
        "宝宝丁/ププリン",
        "波克比/トゲピー",
        "波克基古/トゲチック",
        "天然雀/ネイティ",
        "天然鸟/ネイティオ",
        "咩利羊/メリープ",
        "茸茸羊/モココ",
        "电龙/デンリュウ",
        "美丽花/キレイハナ",
        "玛力露/マリル",
        "玛力露丽/マリルリ",
        "树才怪/ウソッキー",
        "蚊香蛙皇/ニョロトノ",
        "毽子草/ハネッコ",
        "毽子花/ポポッコ",
        "毽子棉/ワタッコ",
        "长尾怪手/エイパム",
        "向日种子/ヒマナッツ",
        "向日花怪/キマワリ",
        "蜻蜻蜓/ヤンヤンマ",
        "乌波/ウパー",
        "沼王/ヌオー",
        "太阳伊布/エーフィ",
        "月亮伊布/ブラッキー",
        "黑暗鸦/ヤミカラス",
        "呆呆王/ヤドキング",
        "梦妖/ムウマ",
        "未知图腾/アンノーン",
        "果然翁/ソーナンス",
        "麒麟奇/キリンリキ",
        "榛果球/クヌギダマ",
        "佛烈托斯/フォレトス",
        "土龙弟弟/ノコッチ",
        "天蝎/グライガー",
        "大钢蛇/ハガネール",
        "布鲁/ブルー",
        "布鲁皇/グランブル",
        "千针鱼/ハリーセン",
        "巨钳螳螂/ハッサム",
        "壶壶/ツボツボ",
        "赫拉克罗斯/ヘラクロス",
        "狃拉/ニューラ",
        "熊宝宝/ヒメグマ",
        "圈圈熊/リングマ",
        "熔岩虫/マグマッグ",
        "熔岩蜗牛/マグカルゴ",
        "小山猪/ウリムー",
        "长毛猪/イノムー",
        "太阳珊瑚/サニーゴ",
        "铁炮鱼/テッポウオ",
        "章鱼桶/オクタン",
        "信使鸟/デリバード",
        "巨翅飞鱼/マンタイン",
        "盔甲鸟/エアームド",
        "戴鲁比/デルビル",
        "黑鲁加/ヘルガー",
        "刺龙王/キングドラ",
        "小小象/ゴマゾウ",
        "顿甲/ドンファン",
        "多边兽2型/ポリゴン２",
        "惊角鹿/オドシシ",
        "图图犬/ドーブル",
        "无畏小子/バルキー",
        "战舞郎/カポエラー",
        "迷唇娃/ムチュール",
        "电击怪/エレキッド",
        "鸭嘴宝宝/ブビィ",
        "大奶罐/ミルタンク",
        "幸福蛋/ハピナス",
        "雷公/ライコウ",
        "炎帝/エンテイ",
        "水君/スイクン",
        "幼基拉斯/ヨーギラス",
        "沙基拉斯/サナギラス",
        "班基拉斯/バンギラス",
        "洛奇亚/ルギア",
        "凤王/ホウオウ",
        "时拉比/セレビィ",

        "OLD_UNOWN_B",
        "OLD_UNOWN_C",
        "OLD_UNOWN_D",
        "OLD_UNOWN_E",
        "OLD_UNOWN_F",
        "OLD_UNOWN_G",
        "OLD_UNOWN_H",
        "OLD_UNOWN_I",
        "OLD_UNOWN_J",
        "OLD_UNOWN_K",
        "OLD_UNOWN_L",
        "OLD_UNOWN_M",
        "OLD_UNOWN_N",
        "OLD_UNOWN_O",
        "OLD_UNOWN_P",
        "OLD_UNOWN_Q",
        "OLD_UNOWN_R",
        "OLD_UNOWN_S",
        "OLD_UNOWN_T",
        "OLD_UNOWN_U",
        "OLD_UNOWN_V",
        "OLD_UNOWN_W",
        "OLD_UNOWN_X",
        "OLD_UNOWN_Y",
        "OLD_UNOWN_Z",

        "木守宫/キモリ",
        "森林蜥蜴/ジュプトル",
        "蜥蜴王/ジュカイン",
        "火稚鸡/アチャモ",
        "力壮鸡/ワカシャモ",
        "火焰鸡/バシャーモ",
        "水跃鱼/ミズゴロウ",
        "沼跃鱼/ヌマクロー",
        "巨沼怪/ラグラージ",
        "土狼犬/ポチエナ",
        "大狼犬/グラエナ",
        "蛇纹熊/ジグザグマ",
        "直冲熊/マッスグマ",
        "刺尾虫/ケムッソ",
        "甲壳茧/カラサリス",
        "狩猎凤蝶/アゲハント",
        "盾甲茧/マユルド",
        "毒粉蛾/ドクケイル",
        "莲叶童子/ハスボー",
        "莲帽小童/ハスブレロ",
        "乐天河童/ルンパッパ",
        "橡实果/タネボー",
        "长鼻叶/コノハナ",
        "狡猾天狗/ダーテング",
        "土居忍士/ツチニン",
        "铁面忍者/テッカニン",
        "脱壳忍者/ヌケニン",
        "傲骨燕/スバメ",
        "大王燕/オオスバメ",
        "蘑蘑菇/キノココ",
        "斗笠菇/キノガッサ",
        "晃晃斑/パッチール",
        "长翅鸥/キャモメ",
        "大嘴鸥/ペリッパー",
        "溜溜糖球/アメタマ",
        "雨翅蛾/アメモース",
        "吼吼鲸/ホエルコ",
        "吼鲸王/ホエルオー",
        "向尾喵/エネコ",
        "优雅猫/エネコロロ",
        "变隐龙/カクレオン",
        "天秤偶/ヤジロン",
        "念力土偶/ネンドール",
        "朝北鼻/ノズパス",
        "煤炭龟/コータス",
        "勾魂眼/ヤミラミ",
        "泥泥鳅/ドジョッチ",
        "鲶鱼王/ナマズン",
        "爱心鱼/ラブカス",
        "龙虾小兵/ヘイガニ",
        "铁螯龙虾/シザリガー",
        "丑丑鱼/ヒンバス",
        "美纳斯/ミロカロス",
        "利牙鱼/キバニア",
        "巨牙鲨/サメハダー",
        "大颚蚁/ナックラー",
        "超音波幼虫/ビブラーバ",
        "沙漠蜻蜓/フライゴン",
        "幕下力士/マクノシタ",
        "铁掌力士/ハリテヤマ",
        "落雷兽/ラクライ",
        "雷电兽/ライボルト",
        "呆火驼/ドンメル",
        "喷火驼/バクーダ",
        "海豹球/タマザラシ",
        "海魔狮/トドグラー",
        "帝牙海狮/トドゼルガ",
        "刺球仙人掌/サボネア",
        "梦歌仙人掌/ノクタス",
        "雪童子/ユキワラシ",
        "冰鬼护/オニゴーリ",
        "月石/ルナトーン",
        "太阳岩/ソルロック",
        "露力丽/ルリリ",
        "跳跳猪/バネブー",
        "噗噗猪/ブーピッグ",
        "正电拍拍/プラスル",
        "负电拍拍/マイナン",
        "大嘴娃/クチート",
        "玛沙那/アサナン",
        "恰雷姆/チャーレム",
        "青绵鸟/チルット",
        "七夕青鸟/チルタリス",
        "小果然/ソーナノ",
        "夜巡灵/ヨマワル",
        "彷徨夜灵/サマヨール",
        "毒蔷薇/ロゼリア",
        "懒人獭/ナマケロ",
        "过动猿/ヤルキモノ",
        "请假王/ケッキング",
        "溶食兽/ゴクリン",
        "吞食兽/マルノーム",
        "热带龙/トロピウス",
        "咕妞妞/ゴニョニョ",
        "吼爆弹/ドゴーム",
        "爆音怪/バクオング",
        "珍珠贝/パールル",
        "猎斑鱼/ハンテール",
        "樱花鱼/サクラビス",
        "阿勃梭鲁/アブソル",
        "怨影娃娃/カゲボウズ",
        "诅咒娃娃/ジュペッタ",
        "饭匙蛇/ハブネーク",
        "猫鼬斩/ザングース",
        "古空棘鱼/ジーランス",
        "可可多拉/ココドラ",
        "可多拉/コドラ",
        "波士可多拉/ボスゴドラ",
        "飘浮泡泡/ポワルン",
        "电萤虫/バルビート",
        "甜甜萤/イルミーゼ",
        "触手百合/リリーラ",
        "摇篮百合/ユレイドル",
        "太古羽虫/アノプス",
        "太古盔甲/アーマルド",
        "拉鲁拉丝/ラルトス",
        "奇鲁莉安/キルリア",
        "沙奈朵/サーナイト",
        "宝贝龙/タツベイ",
        "甲壳龙/コモルー",
        "暴飞龙/ボーマンダ",
        "铁哑铃/ダンバル",
        "金属怪/メタング",
        "巨金怪/メタグロス",
        "雷吉洛克/レジロック",
        "雷吉艾斯/レジアイス",
        "雷吉斯奇鲁/レジスチル",
        "盖欧卡/カイオーガ",
        "固拉多/グラードン",
        "烈空坐/レックウザ",
        "拉帝亚斯/ラティアス",
        "拉帝欧斯/ラティオス",
        "基拉祈/ジラーチ",
        "代欧奇希斯/デオキシス",
        "风铃铃/チリーン",
        "蛋/タマゴ"
         };

    public static class Stats {
        public int hp, atk, def, spa, spd, spe;
        public int total() { return hp + atk + def + spa + spd + spe; }
        public String compact() {
            return "HP " + hp + "  攻 " + atk + "  防 " + def +
                   "  特攻 " + spa + "  特防 " + spd + "  速 " + spe;
        }
    }

    public static class Pokemon {
        public String location;
        public String species;
        public int speciesId;
        public String nature;
        public boolean egg;
        public boolean checksumOk;
        public long personality;
        public Stats ivs;
        public Stats evs;

        public String summary() {
            return location + "  " + species + "  " + nature.split("/")[0] +
                   "\nIV  " + ivs.compact()+
                   "\nEV  " + evs.compact();
        }

        public String detail() {
            StringBuilder sb = new StringBuilder();
            sb.append(species).append("  (内部ID ").append(speciesId).append(")\n");
            sb.append(location).append("\n\n");
            sb.append("性格：").append(nature).append("\n\n");
            sb.append("IV：").append(ivs.compact()).append("\n");
            sb.append("EV：").append(evs.compact()).append("  合计 ").append(evs.total()).append("\n\n");
            sb.append(String.format(Locale.US, "Personality：0x%08X", personality));
            if (egg) sb.append("\n蛋");
            if (!checksumOk) sb.append("\n⚠ 宝可梦校验和异常");
            return sb.toString();
        }
    }

    public static class Result {
        public String gameLabel;
        public String blockName;
        public long saveIndex;
        public List<Pokemon> pokemon = new ArrayList<>();
        public String info() {
            return gameLabel + " · 存档块 " + blockName + " · Save Index " + saveIndex +
                   " · 共 " + pokemon.size() + " 只";
        }
    }

    private static class SaveBlock {
        boolean valid;
        byte[][] sections = new byte[14][];
        long saveIndex;
        List<String> errors = new ArrayList<>();
    }

    private static int u8(byte[] b, int o) { return b[o] & 0xFF; }
    private static int u16(byte[] b, int o) {
        return (b[o] & 0xFF) | ((b[o+1] & 0xFF) << 8);
    }
    private static long u32(byte[] b, int o) {
        return ((long)(b[o] & 0xFF)) |
               ((long)(b[o+1] & 0xFF) << 8) |
               ((long)(b[o+2] & 0xFF) << 16) |
               ((long)(b[o+3] & 0xFF) << 24);
    }
    private static void putU32(byte[] b, int o, long v) {
        b[o]=(byte)v; b[o+1]=(byte)(v>>8); b[o+2]=(byte)(v>>16); b[o+3]=(byte)(v>>24);
    }
    private static byte[] slice(byte[] b, int s, int e) {
        return Arrays.copyOfRange(b, s, e);
    }

    private static int sectionChecksum(byte[] data, int len) {
        long total = 0;
        for (int o=0; o<len; o+=4) total = (total + u32(data,o)) & 0xFFFFFFFFL;
        return (int)(((total >>> 16) + (total & 0xFFFFL)) & 0xFFFFL);
    }

    private static SaveBlock inspectBlock(byte[] raw, int base) {
        SaveBlock out = new SaveBlock();
        boolean[] seen = new boolean[14];
        Long commonIndex = null;
        boolean sameIndex = true;
        int count = 0;

        for (int physical=0; physical<14; physical++) {
            int off = base + physical * SECTION_SIZE;
            if (off + SECTION_SIZE > raw.length) {
                out.errors.add("section truncated");
                continue;
            }
            byte[] sec = slice(raw, off, off+SECTION_SIZE);
            int id = u16(sec,0xFF4);
            int stored = u16(sec,0xFF6);
            long sig = u32(sec,0xFF8);
            long idx = u32(sec,0xFFC);

            if (id < 0 || id >= 14) { out.errors.add("bad section id "+id); continue; }
            if (sig != SIGNATURE) { out.errors.add("bad signature"); continue; }
            int calc = sectionChecksum(sec, SECTION_DATA_SIZES[id]);
            if (calc != stored) { out.errors.add("section "+id+" checksum mismatch"); continue; }
            if (seen[id]) { out.errors.add("duplicate section "+id); continue; }

            seen[id] = true;
            out.sections[id] = sec;
            count++;
            if (commonIndex == null) commonIndex = idx;
            else if (commonIndex.longValue() != idx) sameIndex = false;
        }
        out.valid = count == 14 && sameIndex;
        out.saveIndex = commonIndex == null ? 0 : commonIndex.longValue();
        return out;
    }

    private static boolean newer(long a, long b) {
        long diff = (a - b) & 0xFFFFFFFFL;
        return diff != 0 && diff < 0x80000000L;
    }

    private static SaveBlock[] chooseLatest(byte[] raw) throws Exception {
        SaveBlock a = inspectBlock(raw,0x00000);
        SaveBlock b = inspectBlock(raw,0x0E000);
        if (a.valid && b.valid) return newer(a.saveIndex,b.saveIndex) ? new SaveBlock[]{a} : new SaveBlock[]{b};
        if (a.valid) return new SaveBlock[]{a};
        if (b.valid) return new SaveBlock[]{b};
        throw new Exception("两个存档块都未通过完整校验。请确认是第三世代电池存档 .sav/.srm，而不是即时存档。");
    }

    private static int pokemonChecksum(byte[] d) {
        int sum=0;
        for (int i=0;i<48;i+=2) sum = (sum + u16(d,i)) & 0xFFFF;
        return sum;
    }

    private static Pokemon decodePokemon(byte[] record) {
        if (record.length < 80) return null;
        boolean allZero=true;
        for (int i=0;i<80;i++) if (record[i]!=0) { allZero=false; break; }
        if (allZero) return null;

        long personality = u32(record,0);
        long otId = u32(record,4);
        int storedChecksum = u16(record,0x1C);
        long key = (personality ^ otId) & 0xFFFFFFFFL;

        byte[] dec = new byte[48];
        for (int o=0;o<48;o+=4) {
            long w = (u32(record,0x20+o) ^ key) & 0xFFFFFFFFL;
            putU32(dec,o,w);
        }
        boolean checksumOk = pokemonChecksum(dec) == storedChecksum;

        String order = SUBSTRUCT_ORDERS[(int)(personality % 24)];
        Map<Character,byte[]> subs = new HashMap<>();
        for (int i=0;i<4;i++) subs.put(order.charAt(i), slice(dec,i*12,(i+1)*12));

        byte[] growth = subs.get('G');
        byte[] evcond = subs.get('E');
        byte[] misc = subs.get('M');
        int speciesId = u16(growth,0);
        if (speciesId == 0) return null;

        Pokemon p = new Pokemon();
        p.speciesId = speciesId;
        p.species = (speciesId >= 0 && speciesId < SPECIES.length) ? SPECIES[speciesId] : "Species_"+speciesId;
        p.personality = personality;
        p.nature = NATURES[(int)(personality % 25)];
        p.checksumOk = checksumOk;

        p.evs = new Stats();
        p.evs.hp  = u8(evcond,0);
        p.evs.atk = u8(evcond,1);
        p.evs.def = u8(evcond,2);
        p.evs.spe = u8(evcond,3);
        p.evs.spa = u8(evcond,4);
        p.evs.spd = u8(evcond,5);

        long ivword = u32(misc,4);
        p.ivs = new Stats();
        p.ivs.hp  = (int)((ivword >>> 0) & 31);
        p.ivs.atk = (int)((ivword >>> 5) & 31);
        p.ivs.def = (int)((ivword >>> 10) & 31);
        p.ivs.spe = (int)((ivword >>> 15) & 31);
        p.ivs.spa = (int)((ivword >>> 20) & 31);
        p.ivs.spd = (int)((ivword >>> 25) & 31);
        p.egg = ((ivword >>> 30) & 1) != 0;
        return p;
    }

    private static class PartyCandidate {
        String key,label;
        int count,score;
        List<Pokemon> mons = new ArrayList<>();
    }

    private static PartyCandidate tryParty(byte[] sec1, String key) {
        PartyCandidate c = new PartyCandidate();
        c.key = key;
        int countOff, partyOff, countSize;
        if ("emerald".equals(key)) {
            c.label="R/S/E / Emerald"; countOff=0x234; partyOff=0x238; countSize=4;
        } else {
            c.label="FireRed / LeafGreen"; countOff=0x034; partyOff=0x038; countSize=1;
        }
        long cnt = countSize==1 ? u8(sec1,countOff) : u32(sec1,countOff);
        c.count=(int)cnt;
        if (c.count < 0 || c.count > 6) { c.score=-999; return c; }
        int score = c.count==0 ? 0 : 2;
        for (int i=0;i<c.count;i++) {
            int off=partyOff+i*100;
            if (off+100>sec1.length) { c.score=-999; return c; }
            Pokemon p=decodePokemon(slice(sec1,off,off+100));
            if (p==null) { score-=10; continue; }
            c.mons.add(p);
            score += p.checksumOk ? 10 : -10;
            score += (p.speciesId>=1 && p.speciesId<=412) ? 2 : -5;
        }
        if (c.count>0 && c.mons.size()==c.count) {
            boolean ok=true; for (Pokemon p:c.mons) if(!p.checksumOk) ok=false;
            if(ok) score+=20;
        }
        c.score=score;
        return c;
    }

    private static PartyCandidate detectParty(byte[] sec1) throws Exception {
        PartyCandidate a=tryParty(sec1,"emerald"), b=tryParty(sec1,"frlg");
        PartyCandidate best=a.score>=b.score?a:b, second=best==a?b:a;
        if(best.score<0) throw new Exception("无法自动识别 RSE/FRLG 队伍布局。");
        if(best.score==second.score) throw new Exception("RSE 与 FRLG 队伍布局得分相同，无法可靠自动判断。");
        return best;
    }

    public static Result parse(byte[] fileBytes) throws Exception {
        if (fileBytes.length < SAVE_SIZE)
            throw new Exception("文件小于 128 KiB。请使用 .sav/.srm 电池存档，不要使用 .ss0/.ss1 即时存档。");
        byte[] raw = Arrays.copyOf(fileBytes,SAVE_SIZE);
        SaveBlock block=chooseLatest(raw)[0];

        // identify block name by physical base via reference equality inspection
        SaveBlock a=inspectBlock(raw,0x00000);
        String blockName = (a.valid && a.saveIndex==block.saveIndex && a.sections[0]!=null &&
                Arrays.equals(a.sections[0],block.sections[0])) ? "A" : "B";

        PartyCandidate party=detectParty(block.sections[1]);
        Result r=new Result();
        r.gameLabel=party.label;
        r.blockName=blockName;
        r.saveIndex=block.saveIndex;

        int partyOff = "emerald".equals(party.key) ? 0x238 : 0x038;
        for(int i=0;i<party.count;i++) {
            Pokemon p=decodePokemon(slice(block.sections[1],partyOff+i*100,partyOff+(i+1)*100));
            if(p!=null) {
                p.location="队伍 "+(i+1);
                r.pokemon.add(p);
            }
        }

        byte[] pc=new byte[33744];
        int pos=0;
        for(int id=5;id<=13;id++) {
            int n=SECTION_DATA_SIZES[id];
            System.arraycopy(block.sections[id],0,pc,pos,n);
            pos+=n;
        }
        for(int index=0;index<420;index++) {
            int off=4+index*80;
            Pokemon p=decodePokemon(slice(pc,off,off+80));
            if(p==null || p.speciesId<1 || p.speciesId>412) continue;
            int box=index/30+1, slot=index%30+1;
            p.location=String.format(Locale.US,"箱子 %02d 格 %02d",box,slot);
            r.pokemon.add(p);
        }
        return r;
    }
}
