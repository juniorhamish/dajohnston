"use client";

import type React from "react";
import { useCallback, useEffect, useRef, useState } from "react";
import { cn } from "@/lib/utils";

interface JarQuantitySliderProps {
  value: number;
  onChange: (value: number) => void;
  className?: string;
}

export function JarQuantitySlider({
  value,
  onChange,
  className,
}: Readonly<JarQuantitySliderProps>) {
  const jarRef = useRef<HTMLDivElement>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [internalValue, setInternalValue] = useState(value);
  const commitValueRef = useRef(value);

  const [lastPropValue, setLastPropValue] = useState(value);
  if (value !== lastPropValue) {
    setLastPropValue(value);
    if (!isDragging) {
      setInternalValue(value);
      commitValueRef.current = value;
    }
  }

  const updateValue = useCallback((clientY: number) => {
    if (!jarRef.current) return;
    const rect = jarRef.current.getBoundingClientRect();
    const height = rect.height;
    const y = clientY - rect.top;
    // value is bottom-up, so 0 is bottom, 100 is top
    const newValue = Math.round(
      Math.max(0, Math.min(100, (1 - y / height) * 100)),
    );
    setInternalValue(newValue);
    commitValueRef.current = newValue;
  }, []);

  const handleMouseDown = (e: React.MouseEvent) => {
    setIsDragging(true);
    updateValue(e.clientY);
  };

  const handleTouchStart = (e: React.TouchEvent) => {
    setIsDragging(true);
    updateValue(e.touches[0].clientY);
  };

  useEffect(() => {
    const handleMouseMove = (e: MouseEvent) => {
      if (isDragging) {
        updateValue(e.clientY);
      }
    };

    const handleTouchMove = (e: TouchEvent) => {
      if (isDragging) {
        updateValue(e.touches[0].clientY);
      }
    };

    const handleMouseUp = () => {
      setIsDragging(false);
      onChange(commitValueRef.current);
    };

    const handleTouchEnd = () => {
      setIsDragging(false);
      onChange(commitValueRef.current);
    };

    if (isDragging) {
      globalThis.addEventListener("mousemove", handleMouseMove);
      globalThis.addEventListener("mouseup", handleMouseUp);
      globalThis.addEventListener("touchmove", handleTouchMove);
      globalThis.addEventListener("touchend", handleTouchEnd);
    }

    return () => {
      globalThis.removeEventListener("mousemove", handleMouseMove);
      globalThis.removeEventListener("mouseup", handleMouseUp);
      globalThis.removeEventListener("touchmove", handleTouchMove);
      globalThis.removeEventListener("touchend", handleTouchEnd);
    };
  }, [isDragging, updateValue, onChange]);

  return (
    <div
      ref={jarRef}
      role="slider"
      tabIndex={0}
      aria-valuemin={0}
      aria-valuemax={100}
      aria-valuenow={internalValue}
      className={cn(
        "relative w-20 h-32 border-2 border-primary rounded-t-xl rounded-b-lg overflow-hidden cursor-pointer select-none bg-background touch-none focus:outline-none focus:ring-2 focus:ring-ring",
        className,
      )}
      onMouseDown={handleMouseDown}
      onTouchStart={handleTouchStart}
      onKeyDown={(e) => {
        let newValue: number;
        if (e.key === "ArrowUp" || e.key === "ArrowRight") {
          newValue = Math.min(100, internalValue + 1);
        } else if (e.key === "ArrowDown" || e.key === "ArrowLeft") {
          newValue = Math.max(0, internalValue - 1);
        } else if (e.key === "PageUp") {
          newValue = Math.min(100, internalValue + 10);
        } else if (e.key === "PageDown") {
          newValue = Math.max(0, internalValue - 10);
        } else if (e.key === "Home") {
          newValue = 0;
        } else if (e.key === "End") {
          newValue = 100;
        } else {
          return;
        }
        e.preventDefault();
        setInternalValue(newValue);
        onChange(newValue);
      }}
    >
      {/* Jar Cap Area */}
      <div className="absolute top-0 left-0 right-0 h-4 bg-muted border-b-2 border-primary" />

      {/* Jar Fill */}
      <div
        className="absolute bottom-0 left-0 right-0 bg-primary/80 transition-all duration-75"
        style={{ height: `${internalValue}%` }}
      />

      {/* Glass overlay/reflection */}
      <div className="absolute inset-0 bg-linear-to-r from-white/10 via-transparent to-black/5 pointer-events-none" />

      {/* Value Indicator */}
      <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
        <span
          className={cn(
            "text-xs font-bold",
            internalValue > 50 ? "text-primary-foreground" : "text-primary",
          )}
        >
          {internalValue}%
        </span>
      </div>
    </div>
  );
}
