package eu.hxreborn.tfs.action

import eu.hxreborn.tfs.util.log

class StubAction(
    private val id: ActionId,
) : Action {
    override fun execute() = log("${id.key}: not yet implemented")
}
