import {useCallback, useEffect, useState} from 'react';
import {Loader2, Plus, Trash2} from 'lucide-react';
import {ExtractedRecipeDto, IngredientUnit, RecipeIngredient, RecipeInstruction} from '@/common/types/recipe';
import {useAppDispatch, useAppSelector} from '@/common/hooks/redux';
import {useEscapeKey} from '@/common/hooks/useEscapeKey';
import {
    clearDraft,
    clearUpdateStatus,
    saveRecipe,
    selectSaveStatus,
    selectUpdateStatus,
    updateRecipe,
} from '../recipesSlice';

interface Props {
    recipe: ExtractedRecipeDto;
    // 'create' reviews a freshly extracted draft; 'edit' updates a recipe already in the database.
    mode?: 'create' | 'edit';
    // Required in edit mode: closes the editor and returns to the detail view.
    onClose?: () => void;
}

const EMPTY_INGREDIENT: RecipeIngredient = {
    ingredientName: '',
    quantity: 0,
    unit: IngredientUnit.UNKNOWN,
    size: null,
    preparation: null,
    purpose: null,
    comment: null,
    quantityText: null,
    fullText: '',
    possibleDuplicate: false,
};

const EMPTY_INSTRUCTION: RecipeInstruction = {
    text: '',
    type: 'HowToStep',
    stepCount: 0,
    possibleDuplicate: false,
};

const inputClass =
    'w-full rounded-lg bg-white/5 px-3 py-2 text-sm text-white ring-1 ring-white/10 outline-none placeholder-gray-500 focus:ring-2 focus:ring-indigo-500';

const labelClass = 'text-xs font-semibold uppercase tracking-wider text-gray-500';

/**
 * Edits a recipe in one of two modes. In 'create' mode it shows a freshly extracted recipe for
 * review before anything is stored — saving posts it to the backend, cancelling throws the draft
 * away. In 'edit' mode it changes a stored recipe, patching it on save.
 */
export function RecipeEditorModal({recipe, mode = 'create', onClose}: Props) {
    const dispatch = useAppDispatch();
    const saveStatus = useAppSelector(selectSaveStatus);
    const updateStatus = useAppSelector(selectUpdateStatus);
    const [edited, setEdited] = useState<ExtractedRecipeDto>(recipe);

    const isEditMode = mode === 'edit';
    const status = isEditMode ? updateStatus : saveStatus;
    const isSaving = status === 'loading';

    useEffect(() => {
        setEdited(recipe);
    }, [recipe]);

    const handleCancel = useCallback(() => {
        if (isEditMode) {
            dispatch(clearUpdateStatus());
            onClose?.();
        } else {
            dispatch(clearDraft());
        }
    }, [dispatch, isEditMode, onClose]);

    // Escape is still swallowed mid-save, so it neither closes whatever is underneath nor reaches
    // the browser.
    useEscapeKey(() => {
        if (!isSaving) handleCancel();
    });

    useEffect(() => {
        const prevOverflow = document.body.style.overflow;
        document.body.style.overflow = 'hidden';
        return () => {
            document.body.style.overflow = prevOverflow;
        };
    }, []);

    const setField = <K extends keyof ExtractedRecipeDto>(field: K, value: ExtractedRecipeDto[K]) => {
        setEdited((prev) => ({...prev, [field]: value}));
    };

    const setIngredient = (index: number, changes: Partial<RecipeIngredient>) => {
        setEdited((prev) => ({
            ...prev,
            ingredients: prev.ingredients.map((ing, i) => (i === index ? {...ing, ...changes} : ing)),
        }));
    };

    const removeIngredient = (index: number) => {
        setEdited((prev) => ({
            ...prev,
            ingredients: prev.ingredients.filter((_, i) => i !== index),
        }));
    };

    const addIngredient = () => {
        setEdited((prev) => ({...prev, ingredients: [...prev.ingredients, {...EMPTY_INGREDIENT}]}));
    };

    const setStepText = (index: number, text: string) => {
        setEdited((prev) => ({
            ...prev,
            instructions: prev.instructions.map((step, i) => (i === index ? {...step, text} : step)),
        }));
    };

    const removeStep = (index: number) => {
        setEdited((prev) => ({
            ...prev,
            instructions: prev.instructions.filter((_, i) => i !== index),
        }));
    };

    const addStep = () => {
        setEdited((prev) => ({...prev, instructions: [...prev.instructions, {...EMPTY_INSTRUCTION}]}));
    };

    const handleSave = async () => {
        // The backend needs an ingredient name for every row, so rows the user typed in fall back to
        // their full text. Step order is what the backend numbers the steps from, so it is kept as-is.
        const payload: ExtractedRecipeDto = {
            ...edited,
            ingredients: edited.ingredients
                .filter((ing) => ing.fullText?.trim() || ing.ingredientName?.trim())
                .map((ing) => ({
                    ...ing,
                    ingredientName: ing.ingredientName?.trim() || ing.fullText.trim(),
                })),
            instructions: edited.instructions.filter((step) => step.text?.trim()),
        };

        try {
            if (isEditMode) {
                await dispatch(updateRecipe({id: recipe.id, recipe: payload})).unwrap();
                onClose?.();
            } else {
                await dispatch(saveRecipe(payload)).unwrap();
            }
        } catch {
            // The status renders the failure; the editor stays open so the edits are not lost.
        }
    };

    return (
        <div
            className="fixed inset-0 z-50 flex items-end justify-center overflow-y-auto bg-black/70 p-0 backdrop-blur-sm sm:items-center sm:p-4"
            role="dialog"
            aria-modal="true"
            aria-label={isEditMode ? 'Edit recipe' : 'Review extracted recipe'}
        >
            <div
                className="relative flex max-h-[95vh] w-full flex-col overflow-hidden rounded-t-2xl bg-gray-900 ring-1 ring-white/10 sm:max-w-3xl sm:rounded-2xl lg:max-w-5xl">
                <div className="border-b border-white/5 p-4 sm:px-7">
                    <h2 className="text-base font-semibold text-white">
                        {isEditMode ? 'Edit recipe' : 'Review recipe'}
                    </h2>
                    <p className="mt-0.5 text-xs text-gray-400">
                        {isEditMode
                            ? 'Update anything that is wrong or missing.'
                            : 'Check what was extracted and fix anything that looks wrong before saving.'}
                    </p>
                </div>

                <div className="flex flex-1 flex-col gap-6 overflow-y-auto p-5 sm:p-7">
                    {edited.imageUrl && (
                        <div
                            className="aspect-video w-full shrink-0 overflow-hidden rounded-xl bg-gray-800 sm:aspect-[21/9]">
                            <img src={edited.imageUrl} alt={edited.name} className="h-full w-full object-cover"/>
                        </div>
                    )}

                    <div className="flex flex-col gap-3">
                        <label className="flex flex-col gap-1.5">
                            <span className={labelClass}>Name</span>
                            <input
                                className={inputClass}
                                value={edited.name ?? ''}
                                onChange={(e) => setField('name', e.target.value)}
                            />
                        </label>
                        <label className="flex flex-col gap-1.5">
                            <span className={labelClass}>Description</span>
                            <textarea
                                className={`${inputClass} min-h-20 resize-y`}
                                value={edited.description ?? ''}
                                onChange={(e) => setField('description', e.target.value)}
                            />
                        </label>
                        <label className="flex flex-col gap-1.5">
                            <span className={labelClass}>Image URL</span>
                            <input
                                className={inputClass}
                                value={edited.imageUrl ?? ''}
                                onChange={(e) => setField('imageUrl', e.target.value)}
                            />
                        </label>
                        <label className="flex flex-col gap-1.5">
                            <span className={labelClass}>Source</span>
                            <input className={`${inputClass} text-gray-400`} value={edited.url ?? ''} readOnly/>
                        </label>
                    </div>

                    <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
                        {([
                            ['prepTime', 'Prep time'],
                            ['cookTime', 'Cook time'],
                            ['totalTime', 'Total time'],
                            ['recipeYield', 'Serves'],
                            ['recipeCategory', 'Category'],
                            ['keywords', 'Keywords'],
                        ] as const).map(([field, label]) => (
                            <label key={field} className="flex flex-col gap-1.5">
                                <span className={labelClass}>{label}</span>
                                <input
                                    className={inputClass}
                                    value={edited[field] ?? ''}
                                    onChange={(e) => setField(field, e.target.value)}
                                />
                            </label>
                        ))}
                    </div>

                    <section className="flex flex-col gap-2">
                        <h3 className={labelClass}>Ingredients</h3>
                        {edited.ingredients.map((ing, i) => (
                            <div key={i} className="flex flex-col gap-2 rounded-lg bg-white/5 p-3 ring-1 ring-white/5">
                                <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
                                    <input
                                        className={`${inputClass} sm:flex-1`}
                                        value={ing.fullText ?? ''}
                                        placeholder="1 large onion"
                                        onChange={(e) => setIngredient(i, {fullText: e.target.value})}
                                    />
                                    <div className="flex gap-2">
                                        <input
                                            className={`${inputClass} sm:w-40`}
                                            value={ing.ingredientName ?? ''}
                                            placeholder="onion"
                                            onChange={(e) => setIngredient(i, {ingredientName: e.target.value})}
                                        />
                                        <input
                                            type="number"
                                            className={`${inputClass} w-20`}
                                            value={ing.quantity ?? 0}
                                            onChange={(e) => setIngredient(i, {quantity: Number(e.target.value)})}
                                        />
                                        <select
                                            className={`${inputClass} w-32`}
                                            value={ing.unit ?? IngredientUnit.UNKNOWN}
                                            onChange={(e) => setIngredient(i, {unit: e.target.value as IngredientUnit})}
                                        >
                                            {Object.values(IngredientUnit).map((unit) => (
                                                <option key={unit} value={unit} className="bg-gray-900">
                                                    {unit.toLowerCase()}
                                                </option>
                                            ))}
                                        </select>
                                        <button
                                            onClick={() => removeIngredient(i)}
                                            aria-label="Remove ingredient"
                                            className="flex h-9 w-9 shrink-0 cursor-pointer items-center justify-center rounded-lg bg-white/5 text-gray-400 transition-colors hover:bg-red-500/20 hover:text-red-300"
                                        >
                                            <Trash2 size={16}/>
                                        </button>
                                    </div>
                                </div>
                                {/* The rest of what the ingredient breakdown produced, so nothing is edited blind. */}
                                <div className="grid grid-cols-2 gap-2 sm:grid-cols-5">
                                    {([
                                        ['quantityText', 'Quantity text'],
                                        ['size', 'Size'],
                                        ['preparation', 'Preparation'],
                                        ['purpose', 'Purpose'],
                                        ['comment', 'Comment'],
                                    ] as const).map(([field, placeholder]) => (
                                        <input
                                            key={field}
                                            className={`${inputClass} text-xs`}
                                            value={ing[field] ?? ''}
                                            placeholder={placeholder}
                                            aria-label={placeholder}
                                            onChange={(e) => setIngredient(i, {[field]: e.target.value})}
                                        />
                                    ))}
                                </div>
                            </div>
                        ))}
                        <button
                            onClick={addIngredient}
                            className="flex cursor-pointer items-center justify-center gap-1.5 rounded-lg bg-white/5 px-3 py-2 text-xs font-medium text-gray-300 transition-colors hover:bg-white/10 hover:text-white"
                        >
                            <Plus size={14}/>
                            Add ingredient
                        </button>
                    </section>

                    <section className="flex flex-col gap-2">
                        <h3 className={labelClass}>Steps</h3>
                        {edited.instructions.map((step, i) => (
                            <div key={i} className="flex gap-3">
                                <span
                                    className="mt-1 flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-indigo-600/20 text-xs font-semibold text-indigo-300">
                                    {i + 1}
                                </span>
                                <textarea
                                    className={`${inputClass} min-h-20 flex-1 resize-y`}
                                    value={step.text}
                                    onChange={(e) => setStepText(i, e.target.value)}
                                />
                                <button
                                    onClick={() => removeStep(i)}
                                    aria-label="Remove step"
                                    className="mt-1 flex h-9 w-9 shrink-0 cursor-pointer items-center justify-center rounded-lg bg-white/5 text-gray-400 transition-colors hover:bg-red-500/20 hover:text-red-300"
                                >
                                    <Trash2 size={16}/>
                                </button>
                            </div>
                        ))}
                        <button
                            onClick={addStep}
                            className="flex cursor-pointer items-center justify-center gap-1.5 rounded-lg bg-white/5 px-3 py-2 text-xs font-medium text-gray-300 transition-colors hover:bg-white/10 hover:text-white"
                        >
                            <Plus size={14}/>
                            Add step
                        </button>
                    </section>
                </div>

                <div className="flex items-center justify-end gap-2 border-t border-white/5 p-4 sm:px-7">
                    {status === 'failed' && (
                        <p className="mr-auto text-sm text-red-400">
                            {isEditMode ? 'Could not update the recipe.' : 'Could not save the recipe.'}
                        </p>
                    )}
                    <button
                        onClick={handleCancel}
                        disabled={isSaving}
                        className="cursor-pointer rounded-lg bg-white/5 px-4 py-2.5 text-sm font-medium text-gray-300 transition-colors hover:bg-white/10 hover:text-white disabled:opacity-50"
                    >
                        Cancel
                    </button>
                    <button
                        onClick={handleSave}
                        disabled={isSaving}
                        className="flex cursor-pointer items-center justify-center gap-1.5 rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-medium text-white transition-colors hover:bg-indigo-500 disabled:cursor-not-allowed disabled:bg-gray-700 disabled:text-gray-400"
                    >
                        {isSaving && <Loader2 size={16} className="animate-spin"/>}
                        {isEditMode ? 'Save changes' : 'Save'}
                    </button>
                </div>
            </div>
        </div>
    );
}
