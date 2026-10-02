import type {OptionDto, OptionName} from '../types/options';
import {OPTIONS_ENDPOINT} from '../constants';
import {apiGet, apiPut} from './client';

export function getOption(name: OptionName): Promise<OptionDto> {
    return apiGet<OptionDto>(`${OPTIONS_ENDPOINT}/${name}`);
}

export function updateOption(name: OptionName, value: string): Promise<OptionDto> {
    return apiPut<OptionDto>(`${OPTIONS_ENDPOINT}/${name}`, {value});
}
