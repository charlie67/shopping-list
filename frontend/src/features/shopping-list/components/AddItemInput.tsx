import {useState} from 'react';
import {Plus} from 'lucide-react';
import {useAppDispatch} from '@/common/hooks/redux';
import {addShoppingListItem} from '../shoppingListSlice';

export function AddItemInput() {
    const dispatch = useAppDispatch();
    const [input, setInput] = useState('');

    const handleAdd = () => {
        const title = input.trim();
        if (!title) return;
        dispatch(addShoppingListItem(title));
        setInput('');
    };

    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (e.key === 'Enter') {
            handleAdd();
        }
    };

    return (
        <div
            className="flex items-center gap-3 rounded-xl bg-gray-900 px-4 py-3 ring-1 ring-white/10 focus-within:ring-2 focus-within:ring-indigo-500 transition-shadow">
            <button
                onClick={handleAdd}
                className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-indigo-600 text-white hover:bg-indigo-500 active:bg-indigo-700 transition-colors"
                aria-label="Add item"
            >
                <Plus size={20}/>
            </button>
            <input
                type="text"
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="Add an item..."
                className="flex-1 bg-transparent text-base text-white placeholder-gray-500 outline-none"
            />
        </div>
    );
}
