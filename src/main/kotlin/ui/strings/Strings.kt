package ui.strings

import ui.strings.Language.English
import ui.strings.Language.Japanese
import ui.strings.Language.SimplifiedChinese

enum class Strings(val en: String, val ja: String, val zhCN: String) {
    LanguageDisplayName(
        en = English.displayName,
        ja = Japanese.displayName,
        zhCN = SimplifiedChinese.displayName
    ),
    ReportFeedbackTooltip(
        en = "Send feedback",
        ja = "フィードバックを送信",
        zhCN = "提交反馈"
    ),
    FrequentlyAskedQuestionTooltip(
        en = "Frequently Asked Questions",
        ja = "よくある質問",
        zhCN = "常见问题解答"
    ),
    ConfigTooltip(
        en = "Settings",
        ja = "設定",
        zhCN = "配置"
    ),
    ExporterTitleSuccess(
        en = "Process finished successfully.",
        ja = "処理に成功しました。",
        zhCN = "处理已完成。"
    ),
    ConfirmButton(
        en = "OK",
        ja = "確定",
        zhCN = "确定"
    ),
    CancelButton(
        en = "Cancel",
        ja = "キャンセル",
        zhCN = "取消"
    ),
    ReportButton(
        en = "Report",
        ja = "問題を報告",
        zhCN = "提交报告"
    ),
    ImportFileDescription(
        en = "Drop files or Click to import",
        ja = "ファイルをドロップするか、クリックしてインポート",
        zhCN = "拖放文件或点击导入"
    ),
    ImportFileSubDescription(
        en = "Supported file types: VSQX, VPR, UST, CCS, SVP",
        ja = "サポートされているファイル形式：VSQX、VPR、UST、CCS、SVP",
        zhCN = "支持的文件类型：VSQX、VPR、UST、CCS、SVP"
    ),
    UnsupportedFileTypeImportError(
        en = "Unsupported file type",
        ja = "サポートされていないファイル形式です",
        zhCN = "不支持的文件类型"
    ),
    MultipleFileImportError(
        en = "Multiple files of {{format}} could not be imported in one go",
        ja = "複数の{{format}}ファイルを一度にインポートすることはできません",
        zhCN = "无法同时导入多个{{format}}文件"
    ),
    NoteOverlappingImportError(
        en = "Could not import the project because it contains overlapped notes in track {{trackNumber}}:" +
                " {{notesDescription}}",
        ja = "トラック{{trackNumber}}で下記ノートが重なっているため、インポートできませんでした：{{notesDescription}}",
        zhCN = "无法导入该工程，因为音轨{{trackNumber}}中包含以下重叠的音符：{{notesDescription}}"
    ),
    ImportErrorDialogTitle(
        en = "Failed to import the project",
        ja = "プロジェクトのインポートに失敗しました",
        zhCN = "无法导入该工程"
    ),
    ErrorDialogDescription(
        en = "If you find any problems, please help us collect error information" +
                " for better performance of this application by submitting a feedback report.",
        ja = "サービス向上のためにエラー情報を収集しております。問題を発見した場合、フィードバックにご協力をお願いします。",
        zhCN = "如您在使用中发现问题，您可以向提交反馈表单。感谢您对本应用的改善所提供的帮助！"
    ),
    ReportUrl(
        en = "https://forms.gle/2hEPMQMfDwmQnBht5",
        ja = "https://forms.gle/c7VbmJ8a4i7SKh3M6",
        zhCN = "https://forms.gle/eZKbdmkVj8n4vqJT9"
    ),
    ConfigDescriptionUrl(
        en = "https://gist.github.com/sdercolin/f9c9e1c99e68c947277aeb28e260686c",
        ja = "https://gist.github.com/sdercolin/57ea4642a2acf504d046b89b1a958d3d",
        zhCN = "https://gist.github.com/sdercolin/5e5983dc490fc4060ad4d4c881992b67"
    ),
    FaqUrl(
        en = "https://gist.github.com/sdercolin/24aa542629d8865bd29633c1b06b82be",
        ja = "https://gist.github.com/sdercolin/4eba1351ca1724f4fb7283c431b8249a",
        zhCN = "https://gist.github.com/sdercolin/4f0af177ea1ce0ed71f4645ae692473e"
    ),
    ReleaseNotesUrl(
        en = "https://gist.github.com/sdercolin/37739870bc6430852861c2ff0126afff",
        ja = "https://gist.github.com/sdercolin/37739870bc6430852861c2ff0126afff",
        zhCN = "https://gist.github.com/sdercolin/37739870bc6430852861c2ff0126afff"
    ),
    ExportButton(
        en = "Export",
        ja = "エクスポート",
        zhCN = "导出"
    ),
    RestartButton(
        en = "Back to the beginning",
        ja = "プロジェクトインポート画面に戻る",
        zhCN = "回到初始页面"
    ),
    TrackLabel(
        en = "Track #{{number}}",
        ja = "トラック #{{number}}",
        zhCN = "音轨 #{{number}}"
    ),
    TrackSummary(
        en = "{{barCount}} bars, {{noteCount}} notes",
        ja = "小節数：{{barCount}}、ノート数：{{noteCount}}",
        zhCN = "小节数：{{barCount}}，音符数：{{noteCount}}"
    ),
    MarkTonalityButton(
        en = "Set tonality",
        ja = "調性を指定",
        zhCN = "指定调性"
    ),
    SelectHarmonyButton(
        en = "Select harmony",
        ja = "コーラスを選択",
        zhCN = "选择和声"
    ),
    PassageLabel(
        en = "Section #{{number}}",
        ja = "セクション #{{number}}",
        zhCN = "区间 #{{number}}"
    ),
    StartingBarLabel(
        en = "From Bar",
        ja = "開始小節",
        zhCN = "起始小节"
    ),
    EndingBarLabel(
        en = "To Bar",
        ja = "終了小節",
        zhCN = "结束小节"
    ),
    ProbableTonalitiesDescription(
        en = "Probably: {{options}}",
        ja = "{{options}}のどれか",
        zhCN = "可能为：{{options}}"
    ),
    TonalityLabel(
        en = "Tonality",
        ja = "調性",
        zhCN = "调性"
    ),
    LyricsLabel(
        en = "Lyrics",
        ja = "歌詞",
        zhCN = "歌词"
    ),
    AutoTonalityButton(
        en = "Auto tonality",
        ja = "調性を算出",
        zhCN = "自动分析调性"
    ),
    AutoPassageDivisionButton(
        en = "Auto section division",
        ja = "セクションを自動分割",
        zhCN = "自动切分区间"
    ),
    ApplyToAllTracksButton(
        en = "Apply to all tracks",
        ja = "全てのトラックに適用",
        zhCN = "适用于所有音轨"
    ),
    ResetAllButton(
        en = "Reset",
        ja = "リセット",
        zhCN = "重置"
    ),
    HarmonicTypeCopy(
        en = "copy",
        ja = "コピー",
        zhCN = "副本"
    ),
    HarmonicTypeUpperThird(
        en = "3rd harmony",
        ja = "３度上ハモリ",
        zhCN = "上三度和声"
    ),
    HarmonicTypeLowerThird(
        en = "-3rd harmony",
        ja = "３度下ハモリ",
        zhCN = "下三度和声"
    ),
    HarmonicTypeUpperSixth(
        en = "6th harmony",
        ja = "６度上ハモリ",
        zhCN = "上六度和声"
    ),
    HarmonicTypeLowerSixth(
        en = "-6th harmony",
        ja = "６度下ハモリ",
        zhCN = "下六度和声"
    ),
    HarmonicTypeUpperOctave(
        en = "8th harmony",
        ja = "オク上ハモリ",
        zhCN = "上八度和声"
    ),
    HarmonicTypeLowerOctave(
        en = "-8th harmony",
        ja = "オク下ハモリ",
        zhCN = "下八度和声"
    ),
    AutoTonalityErrorTitle(
        en = "Auto tonality",
        ja = "調性を算出",
        zhCN = "自动分析调性"
    ),
    AutoPassageDivisionErrorTitle(
        en = "Auto section division",
        ja = "セクションを自動分割",
        zhCN = "自动切分区间"
    ),
    ApplyToAllTracksErrorTitle(
        en = "Apply section settings to all tracks",
        ja = "セクション設定を全てのトラックに適用",
        zhCN = "将区间设置适用于所有音轨"
    ),
    ExportErrorTitle(
        en = "Export",
        ja = "エクスポート",
        zhCN = "导出"
    ),
    PassageNotSetMessageBar(
        en = "Please finish setting tonality for all tracks first",
        ja = "全てのトラックに調性を設定してから行ってください",
        zhCN = "请先设置好所有音轨的调性"
    ),
    TrackToShortMessage(
        en = "This track is too short for this process",
        ja = "このトラックは短いので、該当プロセスは行えません",
        zhCN = "该音轨过短，无法执行此操作"
    ),
    ConfigEditorDialogTitle(
        en = "Settings",
        ja = "設定",
        zhCN = "配置"
    ),
    ImportConfigButton(
        en = "Import settings",
        ja = "設定を読み込み",
        zhCN = "导入配置文件"
    ),
    ExportConfigButton(
        en = "Export settings",
        ja = "設定を書き出し",
        zhCN = "导出配置文件"
    ),
    ConfigEditorWarning(
        en = "Please make sure that you understand how it works before you apply changes on a config item. ",
        ja = "設定項目をよく理解した上で変更することをおすすめします。",
        zhCN = "在更改配置项目前，请确保您理解它的含义。"
    ),
    ConfigEditorWarningLearnMore(
        en = "Click Here to Learn More.",
        ja = "詳細はこちら",
        zhCN = "点击查看详情"
    ),
    ConfigParamMinLengthRatioOfNoteForValidBar(
        en = "Minimum percentage of total note lengths for a valid bar (0~1)",
        ja = "有効な小節だと判断するために必要な音符の長さが占める最小割合 (0~1)",
        zhCN = "判断小节有效所需的音符长度所占的最小比例 (0~1)"
    ),
    ConfigParamMinProbabilityForCertainTonality(
        en = "Minimum probability for a certain tonality (0~1)",
        ja = "確実な調性だと判断するために必要な最小確率 (0~1)",
        zhCN = "判断调性确定所需的最小概率 (0~1)"
    ),
    ConfigParamMaxProbabilityDifferenceForSimilarlyCertainTonalities(
        en = "Maximum probability difference for a pair of similarly certain tonalities (0~1)",
        ja = "2つの調性が同様に確実だと判断するために必要な最大確率差 (0~1)",
        zhCN = "判断两个调性相似地确定所需的最大概率差 (0~1)"
    ),
    ConfigParamMinUncertaintyForInvalidAnalysisResult(
        en = "Minimum uncertainty for an invalid tonality analysis result (0~11)",
        ja = "無効な調性の分析結果だと判断するために必要な最小不確定値 (0~11)",
        zhCN = "判断调性分析结果无效所需的最小不确定值 (0~11)"
    ),
    ConfigParamMinScoreForBarBelongingToPassage(
        en = "Minimum score for recognize the next bar as a part of the current section (0~1)",
        ja = "次の小節が現在のセクションの一部だと判断するために必要な最小スコア (0~1)",
        zhCN = "判断下一个小节为当前区间的一部分所需的最小评估值 (0~1)"
    ),
    ConfigParamMinBarCountForPassageAutoDivision(
        en = "Minimum number of bars within a section (1~64)",
        ja = "１つのセクションに含めるべき最小小節数 (1~64)",
        zhCN = "1个区间所应包含的最小小节数 (1~64)"
    ),
    ConfigParamKeyShiftForUpperThirdHarmony(
        en = "Map for \"upper third harmony\"",
        ja = "３度上ハモリの割当",
        zhCN = "\"上三度和声\"的映射表"
    ),
    ConfigParamKeyShiftForLowerThirdHarmony(
        en = "Map for \"lower third harmony\"",
        ja = "３度下ハモリの割当",
        zhCN = "\"下三度和声\"的映射表"
    ),
    ConfigParamValidSolfegeSyllablesInOctave(
        en = "Solfege syllables that are included in the scale",
        ja = "音階内だと判断される階名",
        zhCN = "被判断为在音阶内的唱名"
    ),
    ConfigEditorUseDefaultButton(
        en = "Use default",
        ja = "デフォルト",
        zhCN = "使用默认值"
    ),
    ConfigEditorUseStandardButton(
        en = "Use standard",
        ja = "スタンダード",
        zhCN = "使用标准值"
    ),
    PassageAnalysisWarning(
        en = "Could not get certain results from tonality analysis.",
        ja = "調性計算において完全に確定な結果を得られませんでした。",
        zhCN = "未得到完全确定的调性分析结果。"
    ),
    PassageAnalysisWarningSimilarlyCertain(
        en = "・Section {{number}}: Please choose from {{description}}",
        ja = "・セクション{{number}}：{{description}}の中から１つ選択してください",
        zhCN = "・区间{{number}}：请从{{description}}选择一个结果"
    ),
    PassageAnalysisWarningUnknown(
        en = "・Section {{number}}: Unknown tonality. Please choose a tonality by yourself," +
                " or leave it as it is to ignore this section",
        ja = "・セクション{{number}}：調性を検知できませんでした。ご自分で設定するか、このままにしてコーラスから除外します。",
        zhCN = "・区间{{number}}：无法检测出调性。请自行设置，或者保留当前结果来忽略该区间。"
    );

    fun get(language: Language): String = when (language) {
        English -> en
        Japanese -> ja
        SimplifiedChinese -> zhCN
    }
}

fun string(key: Strings, vararg params: Pair<String, String>): String {
    val options = object {}.asDynamic()
    params.forEach { (key, value) ->
        options[key] = value
    }
    return i18next.t(key.name, options) as String
}
