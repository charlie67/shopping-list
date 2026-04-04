import {CheckSquare, Square, Trash2} from 'lucide-react';
import {useAppDispatch} from '@/common/hooks/redux';
import type {ShoppingListItemDto} from '@/common/types/shopping-list';
import {removeShoppingListItem, toggleItemComplete} from '../shoppingListSlice';

interface Props {
    item: ShoppingListItemDto;
}

export function ShoppingListItemRow({item}: Props) {
    const dispatch = useAppDispatch();

    return (
        <div
            className="group flex items-center gap-3 rounded-xl bg-gray-900 px-4 py-3 ring-1 ring-white/5 transition-colors hover:ring-white/10">
            <button
                onClick={() => dispatch(toggleItemComplete({id: item.id, completed: item.completed}))}
                className="shrink-0 text-gray-400 hover:text-indigo-400 transition-colors cursor-pointer"
                aria-label={item.completed ? 'Mark incomplete' : 'Mark complete'}
            >
                {item.completed ? (
                    <CheckSquare size={22} className="text-indigo-500"/>
                ) : (
                    <Square size={22}/>
                )}
            </button>

            <span
                className={`flex-1 text-base transition-all ${
                    item.completed ? 'text-gray-500 line-through' : 'text-white'
                }`}
            >
        {item.title}
      </span>

            <button
                onClick={() => dispatch(removeShoppingListItem(item))}
                className="shrink-0 text-gray-600 transition-all hover:text-red-400 md:opacity-0 md:group-hover:opacity-100 cursor-pointer"
                aria-label="Delete item"
            >
                <Trash2 size={18}/>
            </button>
        </div>
    );
}
