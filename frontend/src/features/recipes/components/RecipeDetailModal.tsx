import {useEffect} from 'react';
import {ExternalLink, Plus, X} from 'lucide-react';
import {ExtractedRecipeDto} from '@/common/types/recipe';
import {useAppDispatch} from '@/common/hooks/redux';
import {addShoppingListItem} from '@/features/shopping-list/shoppingListSlice';

interface Props {
    recipe: ExtractedRecipeDto;
    onClose: () => void;
}

export function RecipeDetailModal({recipe, onClose}: Props) {
    const dispatch = useAppDispatch();

    useEffect(() => {
        const onKey = (e: KeyboardEvent) => {
            if (e.key === 'Escape') onClose();
        };
        document.addEventListener('keydown', onKey);
        const prevOverflow = document.body.style.overflow;
        document.body.style.overflow = 'hidden';
        return () => {
            document.removeEventListener('keydown', onKey);
            document.body.style.overflow = prevOverflow;
        };
    }, [onClose]);

    const addAllIngredients = () => {
        recipe.ingredients.forEach((ing) => {
            dispatch(addShoppingListItem(ing.fullText));
        });
    };

    const sortedSteps = [...recipe.instructions].sort(
        (a, b) => (a.stepCount ?? 0) - (b.stepCount ?? 0),
    );

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
                                onClick={addAllIngredients}
                                className="flex cursor-pointer items-center gap-1.5 rounded-lg bg-indigo-600/20 px-3 py-2 text-xs font-medium text-indigo-300 transition-colors hover:bg-indigo-600/30 hover:text-indigo-200"
                            >
                                <Plus size={14}/>
                                Add all ingredients
                            </button>
                        </div>

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
        </div>
    );
}