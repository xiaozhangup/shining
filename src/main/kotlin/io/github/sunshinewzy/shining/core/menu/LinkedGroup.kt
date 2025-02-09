package io.github.sunshinewzy.shining.core.menu

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.module.ui.ClickEvent
import taboolib.module.ui.type.Basic
import taboolib.platform.util.isNotAir
import java.util.concurrent.CopyOnWriteArrayList

open class LinkedGroup<T>(title: String) : Basic(title) {

    /** 页数 **/
    var page = 0
        private set

    /** 锁定所有位置 **/
    internal var menuLocked = true

    /** 页面可用位置 **/
    internal val menuSlots = CopyOnWriteArrayList<Int>()

    /** 页面可用元素回调 **/
    internal var elementsCallback: (() -> List<T>) = { CopyOnWriteArrayList() }

    /** 页面可用元素缓存 **/
    internal var elementsCache = emptyList<T>()

    /** 点击事件回调 **/
    internal var elementClickCallback: ((event: ClickEvent, index: Int, item: ItemStack) -> Unit) = { _, _, _ -> }

    /** 元素生成回调 **/
    internal var generateCallback: ((player: Player, element: T) -> List<ItemStack>) = { _, _ -> emptyList() }

    /** 异步元素生成回调 **/
    internal var asyncGenerateCallback: ((player: Player, element: T) -> List<ItemStack>) = { _, _ -> emptyList() }

    /** 页面切换回调 */
    internal var pageChangeCallback: ((player: Player) -> Unit) = { _ -> }

    /** 页面玩家 **/
    private lateinit var player: Player

    /**
     * 是否锁定所有位置
     * 默认为 true
     */
    open fun menuLocked(lockAll: Boolean) {
        this.menuLocked = lockAll
    }

    /**
     * 设置页数
     */
    open fun page(page: Int) {
        this.page = page
    }

    /**
     * 设置可用位置
     */
    open fun slots(slots: List<Int>) {
        this.menuSlots.clear()
        this.menuSlots += slots
    }

    /**
     * 通过抽象字符选择由 map 函数铺设的页面位置
     */
    open fun slotsBy(char: Char) {
        slots(getSlots(char))
    }

    /**
     * 可用元素列表回调
     */
    open fun elements(elements: () -> List<T>) {
        elementsCallback = elements
    }

    /**
     * 元素对应物品生成回调
     */
    open fun onGenerate(async: Boolean = false, callback: (player: Player, element: T) -> List<ItemStack>) {
        if (async) {
            asyncGenerateCallback = callback
        } else {
            generateCallback = callback
        }
    }

    /**
     * 页面构建回调
     */
    open fun onBuild(async: Boolean, callback: (inventory: Inventory) -> Unit) {
        onBuild(async = async) { _, inventory -> callback(inventory) }
    }

    /**
     * 元素点击回调
     */
    open fun onClick(callback: (event: ClickEvent, index: Int, item: ItemStack) -> Unit) {
        elementClickCallback = callback
    }

    /**
     * 设置下一页按钮
     */
    open fun setNextPage(slot: Int, callback: (page: Int, hasNextPage: Boolean) -> ItemStack) {
        // 设置物品
        set(slot) { callback(page, hasNextPage()) }
        // 点击事件
        onClick(slot) {
            if (hasNextPage()) {
                page++
                // 刷新页面
                player.openInventory(build())
                pageChangeCallback(player)
            }
        }
    }

    /**
     * 设置上一页按钮
     */
    open fun setPreviousPage(slot: Int, callback: (page: Int, hasPreviousPage: Boolean) -> ItemStack) {
        // 设置物品
        set(slot) { callback(page, hasPreviousPage()) }
        // 点击事件
        onClick(slot) {
            if (hasPreviousPage()) {
                page--
                // 刷新页面
                player.openInventory(build())
                pageChangeCallback(player)
            }
        }
    }

    /**
     * 切换页面回调
     */
    open fun onPageChange(callback: (player: Player) -> Unit) {
        pageChangeCallback = callback
    }

    /**
     * 是否可以返回上一页
     */
    open fun hasPreviousPage(): Boolean {
        return page > 0
    }

    /**
     * 是否可以前往下一页
     */
    open fun hasNextPage(): Boolean {
        return page < elementsCache.size - 1
    }

    override fun createTitle(): String {
        return title.replace("%p", (page + 1).toString())
    }

    open fun resetElementsCache() {
        elementsCache = elementsCallback()
    }

    /**
     * 构建页面
     */
    override fun build(): Inventory {
        // 更新元素列表缓存
        elementsCache = elementsCallback()

        // 本次页面所使用的元素缓存
        val elementMap = hashMapOf<Int, Pair<Int, ItemStack>>()
        val elementGroup = elementsCache.getOrNull(page)

        /**
         * 构建事件处理函数
         */
        fun processBuild(p: Player, inventory: Inventory, async: Boolean) {
            player = p
            // 生成元素对应物品
            val callback = if (async) asyncGenerateCallback else generateCallback
            if (elementGroup != null) {
                val itemStacks = callback(player, elementGroup)
                itemStacks.forEachIndexed { index, itemStack ->
                    val slot = menuSlots.getOrNull(index) ?: 0
                    elementMap[slot] = index to itemStack
                    if (itemStack.isNotAir()) {
                        inventory.setItem(slot, itemStack)
                    }
                }
            }
        }

        // 生成回调
        selfBuild { p, it -> processBuild(p, it, false) }
        // 生成异步回调
        selfBuild(async = true) { p, it -> processBuild(p, it, true) }
        // 生成点击回调
        selfClick {
            if (menuLocked) {
                it.isCancelled = true
            }
            val pair = elementMap[it.rawSlot] ?: return@selfClick
            elementClickCallback(it, pair.first, pair.second)
        }
        // 构建页面
        return super.build()
    }
    
}