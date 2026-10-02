// The backend keys options by an enum, so the name is one of a known set rather than free text.
export type OptionName = 'PLAN_PORTION_SIZE';

export interface OptionDto {
    name: OptionName;
    // Free-form on the wire whatever the option means, so numeric options are parsed defensively.
    value: string;
}
