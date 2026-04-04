import {Outlet} from 'react-router-dom';
import {NavHeader} from './NavHeader';
import {WebSocketProvider} from '@/websocket/WebSocketProvider';

export function RootLayout() {
    return (
        <div className="flex min-h-screen flex-col bg-gray-950 text-white">
            <NavHeader/>
            <WebSocketProvider>
                <main className="flex-1">
                    <Outlet/>
                </main>
            </WebSocketProvider>
        </div>
    );
}
