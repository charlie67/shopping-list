import {useEffect, useRef} from 'react';

type EscapeHandler = () => void;

// Every open layer (modal, picker, ...) that wants Escape, oldest first. Only the last one — the
// layer visually on top — reacts to the key, so a nested picker closes without also closing the
// modal behind it. Nested layers always mount after their parent, so registration order matches
// stacking order.
const layers: {current: EscapeHandler}[] = [];

const onKeyDown = (e: KeyboardEvent) => {
    if (e.key !== 'Escape') return;
    const top = layers[layers.length - 1];
    if (!top) return;
    // Without this the browser also handles the key, which on macOS drops the window out of
    // fullscreen. Any open layer swallows Escape, even when it chooses to ignore it.
    e.preventDefault();
    top.current();
};

/**
 * Calls `onEscape` when Escape is pressed, but only for the topmost layer using this hook. While
 * any layer is mounted the key is consumed, so it never reaches the browser's own Escape handling.
 * A layer that wants to ignore Escape for now (mid-save, say) should still register and return
 * early from its handler, otherwise the key falls through to the layer underneath.
 */
export function useEscapeKey(onEscape: EscapeHandler) {
    const handler = useRef(onEscape);

    useEffect(() => {
        handler.current = onEscape;
    });

    useEffect(() => {
        const entry = handler;
        layers.push(entry);
        if (layers.length === 1) {
            document.addEventListener('keydown', onKeyDown);
        }
        return () => {
            const index = layers.indexOf(entry);
            if (index !== -1) layers.splice(index, 1);
            if (layers.length === 0) {
                document.removeEventListener('keydown', onKeyDown);
            }
        };
    }, []);
}
