import {useEffect} from 'react';

/**
 * Keeps the screen awake while `enabled` is true, using the Screen Wake Lock API
 * (supported on iOS Safari 16.4+, Chrome, Edge). The lock is automatically
 * released by the browser when the tab is backgrounded, so we re-acquire it when
 * the page becomes visible again.
 */
export function useWakeLock(enabled: boolean = true) {
    useEffect(() => {
        if (!enabled) return;
        if (!('wakeLock' in navigator)) return;

        let sentinel: WakeLockSentinel | null = null;
        let cancelled = false;

        const requestLock = async () => {
            try {
                sentinel = await navigator.wakeLock.request('screen');
            } catch {
                // Request can reject (e.g. tab not visible, low battery); ignore.
            }
        };

        const handleVisibilityChange = () => {
            if (!cancelled && document.visibilityState === 'visible') {
                void requestLock();
            }
        };

        void requestLock();
        document.addEventListener('visibilitychange', handleVisibilityChange);

        return () => {
            cancelled = true;
            document.removeEventListener('visibilitychange', handleVisibilityChange);
            void sentinel?.release().catch(() => {
            });
            sentinel = null;
        };
    }, [enabled]);
}
