package domain

data class ChartDraft(
    val title: String,
    val dataTable: DataTableState,
    val styleState: ChartStyleState,
)

data class ValidatedChartSpec(
    val chartType: ChartType,
    val title: String,
    val data: ChartData,
    val styleState: ChartStyleState,
)

sealed interface ChartValidationState {
    val issues: List<ValidationIssue>
    val invalidRowIds: Set<RowId>
    val invalidPaths: Set<ValidationPath>

    data class Valid(
        override val issues: List<ValidationIssue> = emptyList(),
        val appliedRowCount: Int? = null,
    ) : ChartValidationState {
        override val invalidRowIds: Set<RowId> = emptySet()
        override val invalidPaths: Set<ValidationPath> = emptySet()
    }

    data class Invalid(
        override val issues: List<ValidationIssue>,
    ) : ChartValidationState {
        override val invalidRowIds: Set<RowId> =
            issues
                .asSequence()
                .filter { issue -> issue.severity == ValidationSeverity.ERROR }
                .mapNotNull { issue -> issue.path?.rowId }
                .toSet()
        override val invalidPaths: Set<ValidationPath> =
            issues
                .asSequence()
                .filter { issue -> issue.severity == ValidationSeverity.ERROR }
                .mapNotNull { issue -> issue.path }
                .toSet()
    }
}

data class ChartSession(
    val chartType: ChartType,
    val draft: ChartDraft,
    val validatedSpec: ValidatedChartSpec,
    val validation: ChartValidationState,
    val settings: List<SettingDescriptor>,
    val generatedCode: String,
)
