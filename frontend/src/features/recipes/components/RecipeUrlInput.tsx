import {useState} from 'react';
import {Link2, Loader2} from 'lucide-react';
import {useAppDispatch, useAppSelector} from '@/common/hooks/redux';
import {extractRecipeFromUrl, selectExtractionStatus} from '../recipesSlice';

export function RecipeUrlInput() {
    const dispatch = useAppDispatch();
    const extractionStatus = useAppSelector(selectExtractionStatus);
    const [url, setUrl] = useState('');

    const handleSubmit = () => {
        const trimmed = url.trim();
        if (!trimmed) return;
        dispatch(extractRecipeFromUrl(trimmed));
        setUrl('');
    };

    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (e.key === 'Enter') {
            handleSubmit();
        }
    };

    const isLoading = extractionStatus === 'loading';

    return (
        <div
            className="flex items-center gap-3 rounded-xl bg-gray-900 px-4 py-3 ring-1 ring-white/10 focus-within:ring-2 focus-within:ring-indigo-500 transition-shadow">
            <button
                onClick={handleSubmit}
                disabled={isLoading}
                className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-indigo-600 text-white hover:bg-indigo-500 active:bg-indigo-700 disabled:opacity-50 transition-colors"
                aria-label="Extract recipe"
            >
                {isLoading ? <Loader2 size={20} className="animate-spin"/> : <Link2 size={20}/>}
            </button>
            <input
                type="url"
                value={url}
                onChange={(e) => setUrl(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="Paste a recipe URL..."
                disabled={isLoading}
                className="flex-1 bg-transparent text-base text-white placeholder-gray-500 outline-none disabled:opacity-50"
            />
        </div>
    );
}
