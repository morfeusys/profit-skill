package com.justai.jaicf.profit.scenario

import com.justai.jaicf.channel.yandexalice.AliceEvent
import com.justai.jaicf.channel.yandexalice.AliceIntent
import com.justai.jaicf.channel.yandexalice.activator.alice
import com.justai.jaicf.channel.yandexalice.alice
import com.justai.jaicf.channel.yandexalice.api.alice
import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.profit.Profit
import com.justai.jaicf.profit.ProfitCalculator
import com.justai.jaicf.profit.Stuff
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonLiteral
import kotlinx.serialization.json.content
import kotlinx.serialization.json.int

object MainScenario: Scenario() {
    init {
        state("main") {
            activators {
                event(AliceEvent.START)
                intent(AliceIntent.HELP)
            }

            action {
                reactions.say("Я подскажу, какой товар выгоднее. " +
                        "Например спросите, что выгоднее - 250 грамм за 320 рублей или 320 грамм за 400.")
            }
        }

        state("repeat") {
            activators {
                intent(AliceIntent.REPEAT)
            }

            action {
                request.alice?.run {
                    val reply = state?.user?.get("last_reply")
                    if (reply == null) {
                        reactions.say("А вы еще ничего не спрашивали.")
                        reactions.go("/main")
                    } else {
                        reactions.say(reply.primitive.content)
                        reactions.alice?.endSession()
                    }
                }
            }
        }

        state("profit") {
            activators {
                intent("CALCULATE.PROFIT")
            }

            action {
                activator.alice?.run {
                    val firstAmount = slots["first_amount"]
                    val secondAmount = slots["second_amount"]
                    val firstPrice = slots["first_price"]
                    val secondPrice = slots["second_price"]
                    val firstUnit = slots["first_unit"]
                    val secondUnit = slots["second_unit"] ?: firstUnit

                    val first = Stuff(firstAmount?.value?.int ?: 1, firstPrice!!.value.int, firstUnit!!.value.content)
                    val second = Stuff(secondAmount?.value?.int ?: 1, secondPrice!!.value.int, secondUnit!!.value.content)

                    val profit = try {
                        ProfitCalculator.calculateProfit(first, second)
                    } catch (e: Exception) {
                        reactions.say("Тут сосчитать не могу, извините. Попробуйте еще разок.")
                        return@action
                    }

                    reactions.alice?.endSession()

                    if (profit == null || profit.percent == 0) {
                        reactions.say("Тут разницы в цене вообще нет.")
                    } else {
                        val variant = when {
                            profit.stuff === first -> "Первый"
                            else -> "Второй"
                        }

                        var reply = "$variant вариант выгоднее "

                        reply += when {
                            profit.percent < 10 -> "всего лишь на ${profit.percent}%."
                            profit.percent < 100 -> "на ${profit.percent}%."
                            else -> "на целых ${profit.percent}%."
                        }

                        reactions.alice?.updateUserState("last_reply", JsonLiteral(reply))
                        reactions.say(reply)
                    }
                }
            }
        }

        fallback {
            reactions.say("Извините, не очень понятно. " +
                    "Можно спросить так: что выгоднее, 2 литра за 230 рублей или 3 литра за 400.")
        }
    }
}