import {NavLink} from 'react-router-dom';
import {ChefHat, ShoppingCart} from 'lucide-react';

export function NavHeader() {
    return (
        <header className="sticky top-0 z-50 border-b border-white/10 bg-gray-950/80 backdrop-blur-lg">
            <nav className="mx-auto flex h-14 max-w-5xl items-center justify-between px-4">
        <span className="text-lg font-semibold text-white tracking-tight">
          Shopping List
        </span>
                <div className="flex gap-1">
                    <NavLink
                        to="/"
                        end
                        className={({isActive}) =>
                            `flex items-center gap-2 rounded-lg px-4 py-2 text-sm font-medium transition-colors ${
                                isActive
                                    ? 'bg-white/10 text-white'
                                    : 'text-gray-400 hover:bg-white/5 hover:text-white'
                            }`
                        }
                    >
                        <ShoppingCart size={18}/>
                        <span className="hidden sm:inline">List</span>
                    </NavLink>
                    <NavLink
                        to="/recipes"
                        className={({isActive}) =>
                            `flex items-center gap-2 rounded-lg px-4 py-2 text-sm font-medium transition-colors ${
                                isActive
                                    ? 'bg-white/10 text-white'
                                    : 'text-gray-400 hover:bg-white/5 hover:text-white'
                            }`
                        }
                    >
                        <ChefHat size={18}/>
                        <span className="hidden sm:inline">Recipes</span>
                    </NavLink>
                </div>
            </nav>
        </header>
    );
}
