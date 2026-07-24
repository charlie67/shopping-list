import {useEffect, useState} from 'react';
import {Code2, ExternalLink, Loader2, Pencil, Plus, Trash2, X} from 'lucide-react';
import {ExtractedRecipeDto} from '@/common/types/recipe';
import {useAppDispatch, useAppSelector} from '@/common/hooks/redux';
import {useEscapeKey} from '@/common/hooks/useEscapeKey';
import {useWakeLock} from '@/common/hooks/useWakeLock';
import {clearDeleteStatus, deleteRecipe, selectDeleteStatus} from '../recipesSlice';
import {nutritionFacts} from '../nutrition';
import {IngredientPicker} from './IngredientPicker';
import {RecipeEditorModal} from './RecipeEditorModal';

interface Props {
    recipe: ExtractedRecipeDto;
    onClose: () => void;
}

export function RecipeDetailModal({recipe, onClose}: Props) {
    const dispatch = useAppDispatch();
    const deleteStatus = useAppSelector(selectDeleteStatus);
    const [showPicker, setShowPicker] = useState(false);
    const [isEditing, setIsEditing] = useState(false);
    const [confirmingDelete, setConfirmingDelete] = useState(false);

    const isDeleting = deleteStatus === 'loading';

    useWakeLock();

    // A failed delete must not leave its error behind for the next recipe opened.
    useEffect(() => () => {
        dispatch(clearDeleteStatus());
    }, [dispatch]);

    const cancelDelete = () => {
        setConfirmingDelete(false);
        dispatch(clearDeleteStatus());
    };

    const handleDelete = async () => {
        try {
            await dispatch(deleteRecipe(recipe.id)).unwrap();
            // Closing clears the recipe search param, so the grid does not try to refetch the
            // recipe that has just been deleted.
            onClose();
        } catch {
            // deleteStatus renders the failure; the modal stays open.
        }
    };

    // The editor and the ingredient picker sit on top of this modal and handle Escape themselves,
    // so it only reaches here when neither is open. While the delete confirmation is up, Escape
    // backs out of that instead of closing.
    useEscapeKey(() => {
        if (confirmingDelete) {
            if (!isDeleting) cancelDelete();
            return;
        }
        onClose();
    });

    useEffect(() => {
        const prevOverflow = document.body.style.overflow;
        document.body.style.overflow = 'hidden';
        return () => {
            document.body.style.overflow = prevOverflow;
        };
    }, []);

    const sortedSteps = [...recipe.instructions].sort(
        (a, b) => (a.stepCount ?? 0) - (b.stepCount ?? 0),
    );

    const nutrition = nutritionFacts(recipe);

    return (
        <div
            className="fixed inset-0 z-50 flex items-end justify-center overflow-y-auto bg-black/70 p-0 backdrop-blur-sm sm:items-center sm:p-4"
            onClick={onClose}
            role="dialog"
            aria-modal="true"
            aria-label={recipe.name}
        >
            <div
                className="relative flex max-h-[95vh] w-full flex-col overflow-hidden rounded-t-2xl bg-gray-900 ring-1 ring-white/10 sm:max-w-3xl sm:rounded-2xl lg:max-w-5xl"
                onClick={(e) => e.stopPropagation()}
            >
                <button
                    onClick={onClose}
                    aria-label="Close"
                    className="absolute right-3 top-3 z-10 flex h-9 w-9 items-center justify-center rounded-full bg-black/60 text-white backdrop-blur-sm transition-colors hover:bg-black/80"
                >
                    <X size={18}/>
                </button>

                <div className="flex flex-1 flex-col overflow-y-auto">
                    {recipe.imageUrl && (
                        <div className="aspect-video w-full shrink-0 overflow-hidden bg-gray-800 sm:aspect-[21/9]">
                            <img
                                src={recipe.imageUrl}
                                alt={recipe.name}
                                className="h-full w-full object-cover"
                            />
                        </div>
                    )}

                    <div className="flex flex-col gap-6 p-5 sm:p-7">
                        <div>
                            <h2 className="text-xl font-semibold leading-tight text-white sm:text-2xl">
                                {recipe.name}
                            </h2>
                            {recipe.description && (
                                <p className="mt-2 text-sm text-gray-400">{recipe.description}</p>
                            )}
                            {recipe.extractionMethod && (
                                <span
                                    className="mt-3 inline-flex items-center gap-1.5 rounded-full bg-white/5 px-2.5 py-1 text-xs font-medium text-gray-400 ring-1 ring-white/5"
                                    title="How this recipe was read from the source page"
                                >
                                    <Code2 size={12}/>
                                    Extracted via {recipe.extractionMethod}
                                </span>
                            )}
                        </div>

                        <div className="flex flex-wrap items-center gap-2">
                            <a
                                href={recipe.url}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="flex items-center gap-1.5 rounded-lg bg-white/5 px-3 py-2 text-xs font-medium text-gray-300 transition-colors hover:bg-white/10 hover:text-white"
                            >
                                <ExternalLink size={14}/>
                                View source
                            </a>
                            <button
                                onClick={() => setShowPicker(true)}
                                className="flex cursor-pointer items-center gap-1.5 rounded-lg bg-indigo-600/20 px-3 py-2 text-xs font-medium text-indigo-300 transition-colors hover:bg-indigo-600/30 hover:text-indigo-200"
                            >
                                <Plus size={14}/>
                                Add ingredients
                            </button>
                            <button
                                onClick={() => setIsEditing(true)}
                                className="flex cursor-pointer items-center gap-1.5 rounded-lg bg-white/5 px-3 py-2 text-xs font-medium text-gray-300 transition-colors hover:bg-white/10 hover:text-white"
                            >
                                <Pencil size={14}/>
                                Edit
                            </button>
                            {confirmingDelete ? (
                                <div className="flex flex-wrap items-center gap-2">
                                    <span className="text-xs text-gray-300">Delete this recipe?</span>
                                    <button
                                        onClick={cancelDelete}
                                        disabled={isDeleting}
                                        className="cursor-pointer rounded-lg bg-white/5 px-3 py-2 text-xs font-medium text-gray-300 transition-colors hover:bg-white/10 hover:text-white disabled:opacity-50"
                                    >
                                        Cancel
                                    </button>
                                    <button
                                        onClick={handleDelete}
                                        disabled={isDeleting}
                                        className="flex cursor-pointer items-center gap-1.5 rounded-lg bg-red-600 px-3 py-2 text-xs font-medium text-white transition-colors hover:bg-red-500 disabled:cursor-not-allowed disabled:bg-gray-700 disabled:text-gray-400"
                                    >
                                        {isDeleting && <Loader2 size={14} className="animate-spin"/>}
                                        Delete
                                    </button>
                                </div>
                            ) : (
                                <button
                                    onClick={() => setConfirmingDelete(true)}
                                    className="flex cursor-pointer items-center gap-1.5 rounded-lg bg-white/5 px-3 py-2 text-xs font-medium text-gray-300 transition-colors hover:bg-red-500/20 hover:text-red-300"
                                >
                                    <Trash2 size={14}/>
                                    Delete
                                </button>
                            )}
                            {deleteStatus === 'failed' && (
                                <p className="text-xs text-red-400">Could not delete the recipe.</p>
                            )}
                        </div>

                        {nutrition.length > 0 && (
                            <section>
                                <h3 className="text-xs font-semibold uppercase tracking-wider text-gray-500">
                                    Nutrition
                                </h3>
                                <dl className="mt-3 grid grid-cols-2 gap-2 sm:grid-cols-4">
                                    {nutrition.map((fact) => (
                                        <div
                                            key={fact.label}
                                            className="rounded-lg bg-white/5 px-3 py-2 ring-1 ring-white/5"
                                        >
                                            <dt className="text-[11px] uppercase tracking-wider text-gray-500">
                                                {fact.label}
                                            </dt>
                                            <dd className="mt-0.5 text-sm font-semibold text-white">{fact.value}</dd>
                                        </div>
                                    ))}
                                </dl>
                            </section>
                        )}

                        <div className="grid grid-cols-1 gap-6 lg:grid-cols-[minmax(0,1fr)_minmax(0,1.4fr)] lg:gap-8">
                            <section>
                                <h3 className="text-xs font-semibold uppercase tracking-wider text-gray-500">
                                    Ingredients
                                </h3>
                                {recipe.ingredients.length === 0 ? (
                                    <p className="mt-3 text-sm text-gray-500">No ingredients listed.</p>
                                ) : (
                                    <ul className="mt-3 flex flex-col gap-2">
                                        {recipe.ingredients.map((ing, i) => (
                                            <li
                                                key={`${ing.fullText}-${i}`}
                                                className="rounded-lg bg-white/5 px-3 py-2 text-sm text-gray-200 ring-1 ring-white/5"
                                            >
                                                {ing.fullText}
                                            </li>
                                        ))}
                                    </ul>
                                )}
                            </section>

                            <section>
                                <h3 className="text-xs font-semibold uppercase tracking-wider text-gray-500">
                                    Steps
                                </h3>
                                {sortedSteps.length === 0 ? (
                                    <p className="mt-3 text-sm text-gray-500">No steps listed.</p>
                                ) : (
                                    <ol className="mt-3 flex flex-col gap-3">
                                        {sortedSteps.map((step, i) => (
                                            <li key={i} className="flex gap-3">
                                                <span
                                                    className="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-indigo-600/20 text-xs font-semibold text-indigo-300">
                                                    {step.stepCount || i + 1}
                                                </span>
                                                <p className="pt-0.5 text-sm leading-relaxed text-gray-200">
                                                    {step.text}
                                                </p>
                                            </li>
                                        ))}
                                    </ol>
                                )}
                            </section>
                        </div>
                    </div>
                </div>
            </div>

            {showPicker && (
                <div onClick={(e) => e.stopPropagation()}>
                    <IngredientPicker
                        recipeName={recipe.name}
                        ingredients={recipe.ingredients}
                        onClose={() => setShowPicker(false)}
                    />
                </div>
            )}

            {isEditing && (
                <div onClick={(e) => e.stopPropagation()}>
                    <RecipeEditorModal
                        recipe={recipe}
                        mode="edit"
                        onClose={() => setIsEditing(false)}
                    />
                </div>
            )}
        </div>
    );
}