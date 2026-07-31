package domain

enum class ValidationSeverity {
    ERROR,
    WARNING,
}

enum class ValidationIssueCode {
    TOO_FEW_ROWS,
    MISSING_LABEL_COLUMN,
    MISSING_NUMERIC_COLUMN,
    UNSUPPORTED_COMBINATION,
    BLANK_LABEL,
    MISSING_VALUE,
    INVALID_NUMBER,
    NEGATIVE_VALUE,
}

data class ValidationPath(
    val rowId: RowId? = null,
    val columnId: String? = null,
)

data class ValidationArgument(
    val name: String,
    val value: String,
)

data class ValidationIssue(
    val code: ValidationIssueCode,
    val severity: ValidationSeverity = ValidationSeverity.ERROR,
    val path: ValidationPath? = null,
    val arguments: List<ValidationArgument> = emptyList(),
)

data class ValidationResult(
    val sanitizedTable: DataTableState?,
    val data: ChartData?,
    val issues: List<ValidationIssue> = emptyList(),
    val appliedRowCount: Int? = null,
) {
    val isValid: Boolean
        get() = issues.none { issue -> issue.severity == ValidationSeverity.ERROR }

    val invalidRowIds: Set<RowId>
        get() =
            issues
                .asSequence()
                .filter { issue -> issue.severity == ValidationSeverity.ERROR }
                .mapNotNull { issue -> issue.path?.rowId }
                .toSet()
}

fun List<ValidationIssue>.sortedDeterministically(): List<ValidationIssue> =
    sortedWith(
        compareBy<ValidationIssue>(
            { issue -> issue.path?.rowId?.value ?: Int.MIN_VALUE },
            { issue -> issue.path?.columnId.orEmpty() },
            { issue -> issue.code.name },
            { issue -> issue.severity.name },
            { issue -> issue.arguments.joinToString { argument -> "${argument.name}=${argument.value}" } },
        ),
    )
