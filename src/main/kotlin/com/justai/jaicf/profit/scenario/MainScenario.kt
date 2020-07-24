package com.justai.jaicf.profit.scenario

import com.justai.jaicf.channel.yandexalice.model.AliceEvent
import com.justai.jaicf.channel.yandexalice.model.AliceIntent
import com.justai.jaicf.channel.yandexalice.activator.alice
import com.justai.jaicf.channel.yandexalice.alice
import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.profit.ProfitCalculator
import com.justai.jaicf.profit.model.Product
import com.justai.jaicf.profit.model.toAmount
import kotlinx.serialization.json.content
import kotlinx.serialization.json.double
import kotlinx.serialization.json.float
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
                val reply = context.client["last_reply"] as? String
                if (reply == null) {
                    reactions.say("А вы еще ничего не спрашивали.")
                    reactions.go("/main")
                } else {
                    reactions.say(reply)
                    reactions.alice?.endSession()
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

                    context.session["first"] = Product(firstAmount?.value?.toAmount() ?: 1.0, firstPrice!!.value.int, firstUnit?.value?.content ?: "")
                    context.session["second"] = secondPrice?.let {
                        Product(secondAmount?.value?.toAmount() ?: 1.0, secondPrice.value.int, secondUnit?.value?.content ?: "")
                    }

                    reactions.go("calculate")
                }
            }

            state("calculate") {
                action {
                    val first = context.session["first"] as? Product
                    val second = context.session["second"] as? Product

                    if (second == null) {
                        reactions.say("А с чем сравнить?")
                    } else {
                        val profit = try {
                            ProfitCalculator.calculateProfit(first!!, second)
                        } catch (e: Exception) {
                            reactions.say("Тут сосчитать не могу, извините. Попробуйте еще разок.")
                            return@action
                        }

                        if (profit == null || profit.percent == 0) {
                            reactions.say("Тут разницы в цене вообще нет.")
                        } else {
                            val variant = when {
                                profit.product === first -> "Первый"
                                else -> "Второй"
                            }

                            var reply = "$variant вариант выгоднее "

                            reply += when {
                                profit.percent < 10 -> "всего лишь на ${profit.percent}%."
                                profit.percent < 100 -> "на ${profit.percent}%."
                                else -> "на целых ${profit.percent}%."
                            }

                            context.client["last_reply"] = reply
                            reactions.say(reply)
                            reactions.alice?.endSession()
                        }
                    }
                }
            }

            state("second") {
                activators {
                    intent("SECOND.PRODUCT")
                }

                action {
                    activator.alice?.run {
                        val secondAmount = slots["second_amount"]
                        val secondPrice = slots["second_price"]
                        val secondUnit = slots["second_unit"]

                        val first = context.session["first"] as Product
                        context.session["second"] = Product(
                            secondAmount?.value?.toAmount() ?: 1.0,
                            secondPrice!!.value.int,
                            secondUnit?.value?.content ?: first.unit
                        )

                        reactions.go("../calculate")
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