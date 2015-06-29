package org.gtlp.yasb;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class TranslationTable {
    private static final Map<String, Integer> map = ImmutableMap.<String, Integer>builder().put("airhorn", R.raw.airhorn).put("airporn", R.raw.airporn).put("aye", R.raw.aye).put("butimnotarapper", R.raw.butimnotarapper).put("cmonmate", R.raw.cmonmate).put("codscream", R.raw.codscream).put("crawlinginmyskin", R.raw.crawlinginmyskin).put("damnson", R.raw.damnson).put("darudedankstorm", R.raw.darude_dankstorm).put("dedotadedwam", R.raw.dedotadedwam).put("deeznuts", R.raw.deeznuts).put("dota", R.raw.dota).put("dropthebass", R.raw.dropthebass).put("fuckherrightinthepussy", R.raw.fuckherrightinthepussy).put("gaylord", R.raw.gaylord).put("getnoscoped", R.raw.getnoscoped).put("hardscoper", R.raw.hardscoper).put("intervention", R.raw.intervention).put("justhitmarker", R.raw.justhitmarker).put("momgetthecamera", R.raw.momgetthecamera).put("ohbabyatriple", R.raw.ohbabyatriple).put("ohhhhhh", R.raw.ohhhhhh).put("oooooooohmygooood", R.raw.oooooooohmygooood).put("quaaad", R.raw.quaaad).put("quickscober", R.raw.quickscober).put("sanic", R.raw.sanic).put("scrubb", R.raw.scrubb).put("shotsfired", R.raw.shotsfired).put("skrillexscary", R.raw.skrillexscary).put("smokeweedeveryday", R.raw.smokeweedeveryday).put("spooky", R.raw.spooky).put("spookyskellys", R.raw.spookyskellys).put("surprisemotherfucker", R.raw.surprisemotherfucker).put("tacticalnuke", R.raw.tacticalnuke).put("turndownforwhat", R.raw.turndownforwhat).put("twosad4me", R.raw.twosad4me).put("twosed4airhorn", R.raw.twosed4airhorn).put("weed", R.raw.weed).put("whatchasay", R.raw.whatchasay).put("wombocombo", R.raw.wombocombo).put("wow", R.raw.wow).build();

    protected static Integer getRaw(String key) {
        return map.get(key);
    }
}
