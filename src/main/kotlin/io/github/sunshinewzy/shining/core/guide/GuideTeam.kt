package io.github.sunshinewzy.shining.core.guide

import io.github.sunshinewzy.shining.Shining
import io.github.sunshinewzy.shining.api.namespace.NamespacedId
import io.github.sunshinewzy.shining.core.data.JacksonWrapper
import io.github.sunshinewzy.shining.core.data.database.player.PlayerDatabaseHandler.executePlayerDataContainer
import io.github.sunshinewzy.shining.core.data.database.player.PlayerDatabaseHandler.getDataContainer
import io.github.sunshinewzy.shining.core.lang.getLangListNode
import io.github.sunshinewzy.shining.core.lang.getLangText
import io.github.sunshinewzy.shining.core.lang.item.NamespacedIdItem
import io.github.sunshinewzy.shining.core.lang.sendLangTextWithPrefix
import io.github.sunshinewzy.shining.core.menu.MenuBuilder.onBack
import io.github.sunshinewzy.shining.core.menu.MenuBuilder.openMultiPageMenu
import io.github.sunshinewzy.shining.core.menu.MenuBuilder.openSearchMenu
import io.github.sunshinewzy.shining.core.menu.Search
import io.github.sunshinewzy.shining.objects.SItem.Companion.setLore
import io.github.sunshinewzy.shining.objects.SItem.Companion.setName
import io.github.sunshinewzy.shining.objects.item.ShiningIcon
import io.github.sunshinewzy.shining.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import taboolib.common.LifeCycle
import taboolib.common.platform.SkipTo
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic
import taboolib.platform.util.buildItem
import java.util.*

class GuideTeam(id: EntityID<Int>) : IntEntity(id) {
    var name: String by GuideTeams.name
    var owner: UUID by GuideTeams.owner
    var symbol: ItemStack by GuideTeams.symbol
    
    private var members: JacksonWrapper<HashSet<UUID>> by GuideTeams.members
    private var applicants: JacksonWrapper<HashSet<UUID>> by GuideTeams.applicants


    fun join(player: Player) {
        join(player.uniqueId)
        player.getDataContainer()["guide_team"] = id
    }

    private fun join(uuid: UUID) {
        transaction {
            members.value.let {
                it += uuid
                members = JacksonWrapper(it)
            }
        }
    }

    fun leave(player: Player) {
        leave(player.uniqueId)
        player.getDataContainer().delete(player.uniqueId.toString())
    }

    private fun leave(uuid: UUID) {
        transaction {
            members.value.let {
                it -= uuid
                members = JacksonWrapper(it)
            }
        }
    }

    fun apply(player: Player) {
        apply(player.uniqueId)
        player.getDataContainer()["guide_team_apply"] = id
    }

    private fun apply(uuid: UUID) {
        transaction {
            applicants.value.let {
                it += uuid
                applicants = JacksonWrapper(it)
            }
        }
    }
    
    fun accept(uuid: UUID): Boolean {
        if(!applicants.value.contains(uuid))
            return false
        
        join(uuid)
        uuid.executePlayerDataContainer { 
            it["guide_team"] = id
            it.delete("guide_team_apply")
        }
        
        transaction {
            applicants.value.let {
                it -= uuid
                applicants = JacksonWrapper(it)
            }
        }
        return true
    }
    

    @SkipTo(LifeCycle.ACTIVE)
    companion object : IntEntityClass<GuideTeam>(GuideTeams) {
        const val GUIDE_TEAM = "guide_team"

        private val createTeamItem = NamespacedIdItem(Material.SLIME_BALL, NamespacedId(Shining, "shining_guide-create_team"))
        private val joinTeamItem = NamespacedIdItem(Material.ENDER_PEARL, NamespacedId(Shining, "shining_guide-join_team"))
        private val editTeamNameItem = NamespacedIdItem(Material.NAME_TAG, NamespacedId(Shining, "shining_guide-edit_team_name"))


        private suspend fun create(owner: Player, name: String, symbol: ItemStack): Boolean {
            val container = owner.getDataContainer()
            if(!container[GUIDE_TEAM].isNullOrEmpty()) {
                return false
            }
            
            return newSuspendedTransaction transaction@{
                find { GuideTeams.owner eq owner.uniqueId }.let {
                    if(!it.empty()) {
                        return@transaction false
                    }
                }

                val guideTeam = new {
                    this.owner = owner.uniqueId
                    this.name = name
                    this.symbol = symbol
                    this.members = JacksonWrapper(hashSetOf())
                    this.applicants = JacksonWrapper(hashSetOf())
                }
                container[GUIDE_TEAM] = guideTeam.id
                true
            }
        }


        suspend fun Player.hasGuideTeam(): Boolean {
            return newSuspendedTransaction transaction@{
                getDataContainer()[GUIDE_TEAM]?.toInt()?.let { id ->
                    findById(id)?.let {
                        return@transaction true
                    }
                }
                false
            }
        }

        suspend fun Player.getGuideTeam(): GuideTeam? {
            return newSuspendedTransaction transaction@{
                getDataContainer()[GUIDE_TEAM]?.toInt()?.let { id ->
                    findById(id)?.let {
                        return@transaction it
                    }
                }

                null
            }
        }

        fun Player.setupGuideTeam() {
            openMenu<Basic>(getLangText("menu-shining_guide-team-setup-title")) {
                rows(3)

                map(
                    "",
                    "ooaoooboo"
                )

                set('a', createTeamItem.toLangItem(this@setupGuideTeam))
                set('b', joinTeamItem.toLangItem(this@setupGuideTeam))

                onClick('a') {
                    createGuideTeam()
                }

                onClick('b') {
                    joinGuideTeam()
                }

                onClick(lock = true)
            }
        }


        private fun Player.createGuideTeam(name: String = "", symbol: ItemStack = ItemStack(Material.GRASS_BLOCK)) {
            openMenu<Basic>(getLangText("menu-shining_guide-team-create-title")) {
                rows(3)

                map(
                    "",
                    "obocoodoo"
                )

                set('b', editTeamNameItem.toLangItem(this@createGuideTeam).let { langItem ->
                    langItem.getSectionString("create")?.let { text ->
                        langItem.clone().setLore("", text, "", "&e$name")
                    } ?: langItem
                })
                set('c', symbol.clone().setName(getLangText("menu-shining_guide-team-create-edit_symbol")))
                set('d', createTeamItem.toLangItem(this@createGuideTeam).let { langItem ->
                    langItem.getSectionString("create")?.let { text ->
                        langItem.clone().setLore("", text, "", "&f$name")
                    } ?: langItem
                })

                
                onClick('b') {
                    sendMsg(Shining.prefix, getLangText("menu-shining_guide-team-create-input_name"))
                    
                    PlayerChatSubscriber(this@createGuideTeam, getLangText("menu-shining_guide-team-create-edit_id")) {
                        submit {
                            createGuideTeam(message, symbol)
                        }
                        true
                    }.register()

                    closeInventory()
                }

                onClick('c') {
                    openSearchMenu<ItemStack>(getLangText("menu-shining_guide-team-create-search_symbol-title")) {
                        searchMap { Search.allItemMap }

                        onClick { event, item ->
                            createGuideTeam(name, item)
                        }

                        onGenerate { _, element, _, _ ->
                            element
                        }

                        onBack {
                            createGuideTeam(name, symbol)
                        }
                    }
                }

                onClick('d') {
                    if(name.isNotBlank()) {
                        Shining.scope.launch(Dispatchers.IO) {
                            if(create(this@createGuideTeam, name, symbol)) {
                                sendLangTextWithPrefix("menu-shining_guide-team-create-success", Shining.prefix, name)
                                submit {
                                    closeInventory()
                                    playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                                }
                            } else {
                                sendLangTextWithPrefix("menu-shining_guide-team-create-fail-id_already_exists")
                                playSound(location, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
                            }
                        }
                    } else {
                        sendLangTextWithPrefix("menu-shining_guide-team-create-fail-id_or_name_empty")
                        playSound(location, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
                    }
                }

                onClick(lock = true)
            }
        }

        private fun Player.joinGuideTeam() {
            Shining.scope.launch(Dispatchers.IO) {
                val applyTeam = getDataContainer()["guide_team_apply"]
                val teams = newSuspendedTransaction {
                    val allTeam = all().limit(100)
                    if(applyTeam == null) {
                        allTeam.toList()
                    } else {
                        val applyTeamId = applyTeam.toInt()
                        val list = LinkedList<GuideTeam>()
                        for(theTeam in allTeam) {
                            if(theTeam.id.value == applyTeamId) {
                                list.addFirst(theTeam)
                            } else {
                                list += theTeam
                            }
                        }
                        list
                    }
                }
                
                submit {
                    openMultiPageMenu<GuideTeam>(getLangText("menu-shining_guide-team-join-title")) {
                        elements { teams }

                        var applyTeamElement: GuideTeam? = null
                        onGenerate(async = true) { _, element, index, slot ->
                            if(index == 0 && applyTeam != null && element.id.value == applyTeam.toInt()) {
                                applyTeamElement = element
                                return@onGenerate buildItem(element.symbol) {
                                    this.name = "§f${element.name}"
                                    getLangListNode("menu-shining_guide-team-join-already_apply")
                                        ?.format(element.owner.offlinePlayer.name)
                                        ?.let { lore += it.colored() }
                                    element.members.value.forEach {
                                        lore += "§f${it.offlinePlayer.name}"
                                    }
                                    shiny()
                                }
                            }
                            
                            buildItem(element.symbol) {
                                this.name = "§f${element.name}"
                                getLangListNode("menu-shining_guide-team-join-team_symbol")
                                    ?.format(element.owner.offlinePlayer.name)
                                    ?.let { lore += it.colored() }
                                element.members.value.forEach {
                                    lore += "§f${it.offlinePlayer.name}"
                                }
                            }
                        }

                        onBack {
                            setupGuideTeam()
                        }

                        onClick { event, element ->
                            if(applyTeam == null) {
                                element.apply(this@joinGuideTeam)
                                sendLangTextWithPrefix("menu-shining_guide-team-join-apply-success", Shining.prefix, element.name, element.owner.playerName)
                                closeInventory()
                            } else {
                                openMenu<Basic>(getLangText("menu-shining_guide-team-join-reapply-title")) { 
                                    rows(1)
                                    
                                    map("oooaobooo")
                                    
                                    set('a', buildItem(ShiningIcon.CONFIRM) {
                                        val theApplyTeamElement = applyTeamElement
                                        lore += if(theApplyTeamElement != null) {
                                            getLangText("menu-shining_guide-team-join-reapply-existing_team_name", theApplyTeamElement.name)
                                        } else {
                                            getLangText("menu-shining_guide-team-join-reapply-existing_team_id", applyTeam)
                                        }
                                        getLangListNode("menu-shining_guide-team-join-reapply-cancel_and_apply")
                                            ?.format(element.name)
                                            ?.let { lore += it.colored() }
                                    }) {
                                        element.apply(this@joinGuideTeam)
                                        sendLangTextWithPrefix("menu-shining_guide-team-join-apply-success", Shining.prefix, element.name, element.owner.playerName)
                                        closeInventory()
                                    }
                                    
                                    set('b', ShiningIcon.CANCEL.item) {
                                        joinGuideTeam()
                                    }
                                    
                                    onClick(lock = true)
                                }
                            }
                        }
                        
                    }
                }
            }
        }
    }
    
}