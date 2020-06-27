package com.justai.jaicf.profit

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.catchall.CatchAllActivator
import com.justai.jaicf.activator.event.BaseEventActivator
import com.justai.jaicf.channel.yandexalice.activator.AliceIntentActivator
import com.justai.jaicf.profit.scenario.MainScenario

val skill = BotEngine(
    model = MainScenario.model,
    activators = arrayOf(
        AliceIntentActivator,
        BaseEventActivator,
        CatchAllActivator
    )
)