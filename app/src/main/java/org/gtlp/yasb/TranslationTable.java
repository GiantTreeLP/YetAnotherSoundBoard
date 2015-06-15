package org.gtlp.yasb;

import android.support.v4.util.ArrayMap;

public class TranslationTable {
    private static ArrayMap<String, Integer> map = new ArrayMap<>();

    static {
        map.put("airhorn", R.raw.airhorn);
        map.put("airporn", R.raw.airporn);
        map.put("aye", R.raw.aye);
        map.put("butimnotarapper", R.raw.butimnotarapper);
        map.put("cmonmate", R.raw.cmonmate);
        map.put("codscream", R.raw.codscream);
        map.put("crawlinginmyskin", R.raw.crawlinginmyskin);
        map.put("damnson", R.raw.damnson);
        map.put("darudedankstorm", R.raw.darude_dankstorm);
        map.put("dedotadedwam", R.raw.dedotadedwam);
        map.put("deeznuts", R.raw.deeznuts);
        map.put("dota", R.raw.dota);
        map.put("dropthebass", R.raw.dropthebass);
        map.put("fuckherrightinthepussy", R.raw.fuckherrightinthepussy);
        map.put("gaylord", R.raw.gaylord);
        map.put("getnoscoped", R.raw.getnoscoped);
        map.put("hardscoper", R.raw.hardscoper);
        map.put("intervention", R.raw.intervention);
        map.put("justhitmarker", R.raw.justhitmarker);
        map.put("momgetthecamera", R.raw.momgetthecamera);
        map.put("ohbabyatriple", R.raw.ohbabyatriple);
        map.put("ohhhhhh", R.raw.ohhhhhh);
        map.put("oooooooohmygooood", R.raw.oooooooohmygooood);
        map.put("quaaad", R.raw.quaaad);
        map.put("quickscober", R.raw.quickscober);
        map.put("sanic", R.raw.sanic);
        map.put("scrubb", R.raw.scrubb);
        map.put("shotsfired", R.raw.shotsfired);
        map.put("skrillexscary", R.raw.skrillexscary);
        map.put("smokeweedeveryday", R.raw.smokeweedeveryday);
        map.put("spooky", R.raw.spooky);
        map.put("spookyskellys", R.raw.spookyskellys);
        map.put("surprisemotherfucker", R.raw.surprisemotherfucker);
        map.put("tacticalnuke", R.raw.tacticalnuke);
        map.put("turndownforwhat", R.raw.turndownforwhat);
        map.put("twosad4me", R.raw.twosad4me);
        map.put("twosed4airhorn", R.raw.twosed4airhorn);
        map.put("weed", R.raw.weed);
        map.put("whatchasay", R.raw.whatchasay);
        map.put("wombocombo", R.raw.wombocombo);
        map.put("wow", R.raw.wow);
    }

    protected static Integer getRaw(String key) {
        return map.get(key);
    }
}
