package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class GridStyle(val label: String, val description: String) {
    CLASSIC_INK("水墨黑白", "过去如墨，未来如纸，极简震撼"),
    LIFE_STAGES("人生五阶", "少年、青年、黄金、沉淀、从容五色呈现"),
    GOLDEN_HOUR("流金暮色", "岁月鎏金，温暖沉静的时间脉络")
}

data class LifeStage(
    val name: String,
    val subtitle: String,
    val startYear: Int,
    val endYear: Int, // inclusive
    val color: Color,
    val description: String
) {
    val totalWeeks: Int get() = (endYear - startYear + 1) * 52
}

object LifeStages {
    val stages = listOf(
        LifeStage("少年探索", "0 - 18岁", 0, 17, StageChildhood, "求知、纯真、好奇心与世界初相识"),
        LifeStage("青年启程", "18 - 25岁", 18, 24, StageYouth, "高考、大学、独立生活与价值观形成"),
        LifeStage("黄金拼搏", "25 - 50岁", 25, 49, StagePrime, "职业登峰、成家立业、创造核心价值"),
        LifeStage("岁月沉淀", "50 - 65岁", 50, 64, StageMaturity, "阅历通达、中流砥柱、见证下一代飞翔"),
        LifeStage("智慧从容", "65 - 90岁", 65, 89, StageWisdom, "归真自如、笑看风云、安享生命余晖")
    )

    fun getStageForYear(year: Int): LifeStage {
        return stages.find { year in it.startYear..it.endYear } ?: stages.last()
    }
}

data class LifeStats(
    val totalWeeks: Int,
    val passedWeeks: Int,
    val remainingWeeks: Int,
    val passedPercentage: Float,
    val passedDays: Long,
    val remainingDays: Long,
    val remainingWeekends: Long,
    val currentAgeYears: Int,
    val currentAgeWeeks: Int,
    val currentYearWeeksPassed: Int,
    val remainingSummers: Int,
    val estimatedBooksRead: Int,
    val estimatedFamilyVisitsRemaining: Int
)

data class PhilosophicalQuote(
    val text: String,
    val author: String,
    val context: String
)

object LifeQuotes {
    val quotes = listOf(
        PhilosophicalQuote(
            "我们并非拥有太少的时间，而是荒废了太多。生命足够长，足以成就伟大。",
            "塞涅卡 (Seneca)",
            "古罗马斯多葛学派哲学家《论生命之短暂》"
        ),
        PhilosophicalQuote(
            "记住你即将死去，是我所知避免陷入畏惧失去的最好方法。你已经一无所有，没有理由不跟随你的心。",
            "史蒂夫·乔布斯 (Steve Jobs)",
            "斯坦福大学毕业典礼演讲"
        ),
        PhilosophicalQuote(
            "人生天地之间，若白驹过隙，忽然而已。",
            "庄子",
            "《庄子·知北游》"
        ),
        PhilosophicalQuote(
            "每个人的一生只有大约 4,000 个星期。当你看着这一张纸上的格子，时间不再是抽象的数字，而是真实流逝的生命。",
            "蒂姆·厄本 (Tim Urban)",
            "Wait But Why 创始人"
        ),
        PhilosophicalQuote(
            "人生不售来回票，一旦动身，绝不能复返。",
            "罗曼·罗兰",
            "法国著名作家"
        ),
        PhilosophicalQuote(
            "盛年不重来，一日难再晨。及时当勉励，岁月不待人。",
            "陶渊明",
            "《杂诗》"
        ),
        PhilosophicalQuote(
            "世界上只有一种真正的英雄主义，那就是认清生活的真相后依然热爱生活。",
            "罗曼·罗兰",
            "《米开朗琪罗传》"
        ),
        PhilosophicalQuote(
            "我生来一无所有，也将一无所有地离去。唯有此刻，这正在跳动的一周，属于我。",
            "杨绛",
            "《走在人生边上》"
        ),
        PhilosophicalQuote(
            "不要为明天忧虑，因为明天自有明天的忧虑；一天的难处一天当就够了。",
            "纪伯伦",
            "《先知》"
        )
    )
}
