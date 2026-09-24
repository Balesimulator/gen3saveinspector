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
        "勤奋/がんばりや/Hardy",
        "怕寂寞/さみしがり/Lonely",
        "勇敢/ゆうかん/Brave",
        "固执/いじっぱり/Adamant",
        "顽皮/やんちゃ/Naughty",
        "大胆/ずぶとい/Bold",
        "坦率/すなお/Docile",
        "悠闲/のんき/Relaxed",
        "淘气/わんぱく/Impish",
        "乐天/のうてんき/Lax",
        "胆小/おくびょう/Timid",
        "急躁/せっかち/Hasty",
        "认真/まじめ/Serious",
        "爽朗/ようき/Jolly",
        "天真/むじゃき/Naive",
        "内敛/ひかえめ/Modest",
        "慢吞吞/おっとり/Mild",
        "冷静/れいせい/Quiet",
        "害羞/てれや/Bashful",
        "马虎/うっかりや/Rash",
        "温和/おだやか/Calm",
        "温顺/おとなしい/Gentle",
        "自大/なまいき/Sassy",
        "慎重/しんちょう/Careful",
        "浮躁/きまぐれ/Quirky"
    };

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
                   "\nIV  " + ivs.compact();
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
