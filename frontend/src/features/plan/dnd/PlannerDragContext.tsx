import {createContext, useCallback, useContext, useMemo, useState, type ReactNode} from 'react';
import type {DragPayload, DropTarget} from './plannerDnd';

/**
 * Drag and drop for the planner, on the browser's own HTML5 drag events.
 *
 * Deliberately desktop only: native drag events do not fire on touch, so a phone gets no drag at
 * all and every gesture goes through the placement sheet instead. That is the intended behaviour -
 * dragging is a mouse shortcut, and the sheet is the way the planner is actually used on a phone.
 *
 * What is being carried lives here rather than in `dataTransfer` because the payload is a whole
 * batch, and `dataTransfer.getData` is deliberately unreadable during `dragover` - the moment a drop
 * zone needs to know whether to light up. `dataTransfer` is still set, so the browser shows a real
 * drag image and a move cursor.
 */

interface PlannerDrag {
    dragging: DragPayload | null;
    start: (payload: DragPayload) => void;
    end: () => void;
    drop: (target: DropTarget) => void;
}

const PlannerDragCtx = createContext<PlannerDrag | null>(null);

interface Props {
    onDrop: (payload: DragPayload, target: DropTarget) => void;
    children: ReactNode;
}

export function PlannerDragProvider({onDrop, children}: Props) {
    const [dragging, setDragging] = useState<DragPayload | null>(null);

    // Read outside the state updater on purpose: an updater has to stay pure, or StrictMode's double
    // invocation would fire the drop twice.
    const drop = useCallback((target: DropTarget) => {
        if (dragging) onDrop(dragging, target);
        setDragging(null);
    }, [dragging, onDrop]);

    const value = useMemo<PlannerDrag>(() => ({
        dragging,
        start: setDragging,
        end: () => setDragging(null),
        drop,
    }), [dragging, drop]);

    return <PlannerDragCtx.Provider value={value}>{children}</PlannerDragCtx.Provider>;
}

export function usePlannerDrag(): PlannerDrag {
    const context = useContext(PlannerDragCtx);
    if (context === null) {
        throw new Error('usePlannerDrag must be used inside a PlannerDragProvider');
    }
    return context;
}
