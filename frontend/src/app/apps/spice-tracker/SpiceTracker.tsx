"use client";

import { ChevronDown, ChevronRight, Loader2, Plus, Trash2 } from "lucide-react";
import { useOptimistic, useState, useTransition } from "react";
import { JarQuantitySlider } from "@/components/apps/spice-tracker/JarQuantitySlider";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { cn } from "@/lib/utils";
import {
  addPantryJarAction,
  createSpiceAction,
  removePantryJarAction,
  removeSpiceAction,
  updatePantryJarAction,
} from "./spice-actions";

interface Spice {
  id: string;
  name: string;
}

interface Jar {
  id: string;
  spiceId: string;
  spiceName: string;
  quantity: number;
}

interface SpiceTrackerProps {
  spices: Spice[];
  jars: Jar[];
}

export default function SpiceTracker({
  spices,
  jars,
}: Readonly<SpiceTrackerProps>) {
  const [isAddingSpice, setIsAddingSpice] = useState(false);
  const [isAddingJar, setIsAddingJar] = useState(false);
  const [isPending, startTransition] = useTransition();
  const [error, setError] = useState<string | null>(null);

  const [optimisticJars, updateOptimisticJars] = useOptimistic(
    jars,
    (state, updatedJar: { id: string; quantity: number }) =>
      state.map((j) =>
        j.id === updatedJar.id ? { ...j, quantity: updatedJar.quantity } : j,
      ),
  );

  const [expandedSpices, setExpandedSpices] = useState<Record<string, boolean>>(
    {},
  );

  const toggleExpand = (spiceId: string) => {
    setExpandedSpices((prev) => ({ ...prev, [spiceId]: !prev[spiceId] }));
  };

  const sortedSpices = [...spices].sort((a, b) => a.name.localeCompare(b.name));

  const groupedJars = sortedSpices.map((spice) => {
    const spiceJars = optimisticJars
      .filter((jar) => jar.spiceId === spice.id)
      .sort((a, b) => a.id.localeCompare(b.id));
    const totalQuantity = spiceJars.reduce((sum, jar) => sum + jar.quantity, 0);
    return {
      ...spice,
      jars: spiceJars,
      totalQuantity,
    };
  });

  const getStatusColor = (total: number) => {
    if (total < 20) return "bg-destructive";
    if (total < 50) return "bg-yellow-500";
    return "bg-green-500";
  };

  const handleAddSpice = async (formData: FormData) => {
    const name = formData.get("name") as string;
    setError(null);
    startTransition(async () => {
      try {
        await createSpiceAction(name);
        setIsAddingSpice(false);
      } catch (error) {
        if (error instanceof Error) {
          setError(error.message);
        }
      }
    });
  };

  const handleAddJar = async (formData: FormData) => {
    const spiceId = formData.get("spiceId") as string;
    const quantity = Number.parseInt(formData.get("quantity") as string, 10);
    setError(null);
    startTransition(async () => {
      try {
        await addPantryJarAction(spiceId, quantity);
        setIsAddingJar(false);
      } catch (error) {
        if (error instanceof Error) {
          setError(error.message);
        }
      }
    });
  };

  const handleUpdateJar = (jarId: string, quantity: number) => {
    startTransition(async () => {
      updateOptimisticJars({ id: jarId, quantity });
      try {
        await updatePantryJarAction(jarId, quantity);
      } catch (e) {
        console.error(e);
      }
    });
  };

  const handleRemoveJar = (jarId: string) => {
    if (!confirm("Are you sure you want to remove this jar?")) return;
    startTransition(async () => {
      try {
        await removePantryJarAction(jarId);
      } catch (e) {
        console.error(e);
      }
    });
  };

  const handleRemoveSpice = (spiceId: string) => {
    if (
      !confirm(
        "Are you sure you want to delete this spice? This will remove all associated jars as well.",
      )
    )
      return;
    startTransition(async () => {
      try {
        await removeSpiceAction(spiceId);
      } catch (e) {
        console.error(e);
      }
    });
  };

  return (
    <div className="w-full space-y-6">
      <div className="flex gap-4">
        <Button onClick={() => setIsAddingSpice(true)}>
          <Plus className="mr-2 h-4 w-4" /> Add Spice
        </Button>
        <Button variant="outline" onClick={() => setIsAddingJar(true)}>
          <Plus className="mr-2 h-4 w-4" /> Add Jar to Pantry
        </Button>
      </div>

      {isAddingSpice && (
        <Card className="max-w-md">
          <CardHeader>
            <CardTitle>Add New Spice</CardTitle>
          </CardHeader>
          <CardContent>
            <form action={handleAddSpice} className="space-y-4">
              <div className="space-y-2">
                <label htmlFor="spice-name" className="text-sm font-medium">
                  Spice Name
                </label>
                <input
                  id="spice-name"
                  name="name"
                  required
                  className="w-full px-3 py-2 border rounded-md bg-background"
                  placeholder="e.g. Cumin"
                />
              </div>
              {error && <p className="text-sm text-destructive">{error}</p>}
              <div className="flex gap-2">
                <Button type="submit" disabled={isPending}>
                  {isPending ? (
                    <Loader2 className="animate-spin mr-2 h-4 w-4" />
                  ) : null}
                  Create Spice
                </Button>
                <Button
                  variant="ghost"
                  type="button"
                  onClick={() => {
                    setIsAddingSpice(false);
                    setError(null);
                  }}
                >
                  Cancel
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      )}

      {isAddingJar && (
        <Card className="max-w-md">
          <CardHeader>
            <CardTitle>Add Jar to Pantry</CardTitle>
          </CardHeader>
          <CardContent>
            <form action={handleAddJar} className="space-y-4">
              <div className="space-y-2">
                <label htmlFor="spice-select" className="text-sm font-medium">
                  Select Spice
                </label>
                <select
                  id="spice-select"
                  name="spiceId"
                  required
                  className="w-full px-3 py-2 border rounded-md bg-background"
                >
                  <option value="">-- Choose a Spice --</option>
                  {spices.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.name}
                    </option>
                  ))}
                </select>
              </div>
              <div className="space-y-2">
                <label
                  htmlFor="initial-quantity"
                  className="text-sm font-medium"
                >
                  Initial Quantity (%)
                </label>
                <input
                  id="initial-quantity"
                  name="quantity"
                  type="number"
                  min="0"
                  max="100"
                  defaultValue="100"
                  required
                  className="w-full px-3 py-2 border rounded-md bg-background"
                />
              </div>
              {error && <p className="text-sm text-destructive">{error}</p>}
              <div className="flex gap-2">
                <Button type="submit" disabled={isPending}>
                  {isPending ? (
                    <Loader2 className="animate-spin mr-2 h-4 w-4" />
                  ) : null}
                  Add Jar
                </Button>
                <Button
                  variant="ghost"
                  type="button"
                  onClick={() => setIsAddingJar(false)}
                >
                  Cancel
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {groupedJars.map((spice) => (
          <Card key={spice.id} className="overflow-hidden">
            <div className="flex items-center hover:bg-accent/50 transition-colors">
              <button
                type="button"
                className="flex-1 p-4 flex items-center justify-between cursor-pointer text-left"
                onClick={() => toggleExpand(spice.id)}
              >
                <div className="flex items-center gap-3">
                  <div
                    className={cn(
                      "w-3 h-3 rounded-full",
                      getStatusColor(spice.totalQuantity),
                    )}
                  />
                  <CardTitle className="text-lg">{spice.name}</CardTitle>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-sm text-muted-foreground font-medium">
                    {spice.totalQuantity}% total
                  </span>
                  {expandedSpices[spice.id] ? (
                    <ChevronDown className="h-4 w-4 text-muted-foreground" />
                  ) : (
                    <ChevronRight className="h-4 w-4 text-muted-foreground" />
                  )}
                </div>
              </button>
              <div className="pr-4">
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-8 w-8 text-muted-foreground hover:text-destructive hover:bg-destructive/10"
                  onClick={() => handleRemoveSpice(spice.id)}
                  aria-label={`Delete ${spice.name}`}
                >
                  <Trash2 className="h-4 w-4" />
                </Button>
              </div>
            </div>

            {expandedSpices[spice.id] && (
              <CardContent className="border-t bg-muted/30 p-4 space-y-4">
                {spice.jars.length === 0 ? (
                  <p className="text-sm text-muted-foreground italic text-center py-2">
                    No jars in pantry
                  </p>
                ) : (
                  spice.jars.map((jar) => (
                    <div
                      key={jar.id}
                      className="flex items-center gap-4 bg-background p-3 rounded-lg border shadow-sm"
                    >
                      <JarQuantitySlider
                        value={jar.quantity}
                        onChange={(val) => handleUpdateJar(jar.id, val)}
                        className="w-12 h-20"
                      />
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium truncate">
                          Jar ({jar.quantity}%)
                        </p>
                        <p className="text-xs text-muted-foreground">
                          Drag jar to adjust
                        </p>
                      </div>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="text-destructive hover:text-destructive hover:bg-destructive/10"
                        onClick={() => handleRemoveJar(jar.id)}
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  ))
                )}
              </CardContent>
            )}
          </Card>
        ))}
      </div>
    </div>
  );
}
