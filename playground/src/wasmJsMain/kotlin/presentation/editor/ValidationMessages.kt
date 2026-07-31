package presentation.editor

import domain.ChartValidationState
import domain.ValidationIssue
import domain.ValidationIssueCode

internal fun ChartValidationState.presentationMessage(): String? {
    val issueMessage = issues.joinToString(separator = " ") { issue -> issue.presentationMessage() }
    return when (this) {
        is ChartValidationState.Valid ->
            listOfNotNull(
                issueMessage.takeIf { it.isNotBlank() },
                appliedRowCount?.let { count -> "Applied $count rows." },
            ).joinToString(" ").takeIf { it.isNotBlank() }
        is ChartValidationState.Invalid -> issueMessage.ifBlank { "Please review the highlighted values." }
    }
}

private fun ValidationIssue.presentationMessage(): String =
    when (code) {
        ValidationIssueCode.TOO_FEW_ROWS ->
            "${argument("chart")} needs at least ${argument("minimum")} rows."
        ValidationIssueCode.MISSING_LABEL_COLUMN -> "A label column is required."
        ValidationIssueCode.MISSING_NUMERIC_COLUMN -> "At least one numeric column is required."
        ValidationIssueCode.UNSUPPORTED_COMBINATION ->
            "This table configuration is not supported for the selected chart."
        ValidationIssueCode.BLANK_LABEL -> "Blank labels will use generated labels."
        ValidationIssueCode.MISSING_VALUE -> "Enter a value in every highlighted numeric cell."
        ValidationIssueCode.INVALID_NUMBER -> "Enter valid numeric values in every highlighted cell."
        ValidationIssueCode.NEGATIVE_VALUE -> "Values must be non-negative in the highlighted cells."
    }

private fun ValidationIssue.argument(name: String): String =
    arguments.firstOrNull { argument -> argument.name == name }?.value.orEmpty()
