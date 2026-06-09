package com.campusevent.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class EventTypes {
    private static final List<String> TYPES = Collections.unmodifiableList(Arrays.asList(
            "講座",
            "參訪(含校外、產業、場域、實地參訪)",
            "工作坊",
            "研討會",
            "成果展",
            "監試報名",
            "論壇",
            "競賽",
            "座談會",
            "展覽",
            "教育訓練課程",
            "評圖",
            "工作營",
            "聯誼交流活動(畢業茶會、系友會)",
            "測驗",
            "其他"
    ));

    private EventTypes() {
    }

    public static List<String> getTypes() {
        return TYPES;
    }
}
