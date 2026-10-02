import {useRef, useState, type DragEvent} from 'react';
import type {DragPayload, DropTarget} from './plannerDnd';
import {usePlannerDrag} from './PlannerDragContext';

/** Makes one meal draggable with the browser's own drag events. */
export function useMealDrag(payload: DragPayload, label: string) {
    const {dragging, start, end} = usePlannerDrag();
    const [isDragging, setIsDragging] = useState(false);

    return {
        isDragging: isDragging && dragging !== null,
        dragProps: {
            draggable: true,
            onDragStart: (event: DragEvent) => {
                // Firefox starts no drag at all unless dataTransfer carries something.
                event.dataTransfer.setData('text/plain', label);
                event.dataTransfer.effectAllowed = 'move';
                setIsDragging(true);
                start(payload);
            },
            onDragEnd: () => {
                setIsDragging(false);
                end();
            },
        },
    };
}

export function useMealDrop(target: DropTarget) {
    const {dragging, drop} = usePlannerDrag();
    const [isOver, setIsOver] = useState(false);
    // dragenter and dragleave fire again for every child element the pointer crosses, so the zone is
    // only really left once the enters and leaves balance out.
    const depth = useRef(0);

    const reset = () => {
        depth.current = 0;
        setIsOver(false);
    };

    return {
        isOver: isOver && dragging !== null,
        isDragActive: dragging !== null,
        dropProps: {
            onDragEnter: (event: DragEvent) => {
                if (dragging === null) return;
                event.preventDefault();
                depth.current += 1;
                setIsOver(true);
            },
            // Without preventDefault on every dragover the browser refuses the drop outright.
            onDragOver: (event: DragEvent) => {
                if (dragging === null) return;
                event.preventDefault();
                event.dataTransfer.dropEffect = 'move';
            },
            onDragLeave: () => {
                depth.current -= 1;
                if (depth.current <= 0) reset();
            },
            onDrop: (event: DragEvent) => {
                if (dragging === null) return;
                event.preventDefault();
                reset();
                drop(target);
            },
        },
    };
}
