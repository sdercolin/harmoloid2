package ui.external.materialui

import react.RClass
import react.RProps

@JsModule("@material-ui/icons")
@JsNonModule
private external val iconModule: dynamic

object Icons {
    val save = iconModule.SaveAlt.unsafeCast<RClass<IconProps>>()
    val refresh = iconModule.Refresh.unsafeCast<RClass<IconProps>>()
    val language = iconModule.Language.unsafeCast<RClass<IconProps>>()
    val feedback = iconModule.Feedback.unsafeCast<RClass<IconProps>>()
    val arrowBack = iconModule.ArrowBack.unsafeCast<RClass<IconProps>>()
    val liveHelp = iconModule.LiveHelp.unsafeCast<RClass<IconProps>>()
    val edit = iconModule.Edit.unsafeCast<RClass<IconProps>>()
    val checkCircle = iconModule.CheckCircle.unsafeCast<RClass<IconProps>>()
    val addCircle = iconModule.AddCircle.unsafeCast<RClass<IconProps>>()
    val removeCircle = iconModule.RemoveCircle.unsafeCast<RClass<IconProps>>()
    val replay = iconModule.Replay.unsafeCast<RClass<IconProps>>()
    val check = iconModule.Check.unsafeCast<RClass<IconProps>>()
    val settings = iconModule.Settings.unsafeCast<RClass<IconProps>>()
    val fetch = iconModule.GetApp.unsafeCast<RClass<IconProps>>()
    val publish = iconModule.Publish.unsafeCast<RClass<IconProps>>()
}

external interface IconProps : RProps {
    var color: String
    var style: Style
}
