import { useTheme } from 'next-themes';
import { Sun, Moon } from 'lucide-react';
import { Button } from '@/components/ui/button';

export const ThemeToggle = () => {
  const { theme, setTheme } = useTheme();

  const toggleTheme = () => {
    setTheme(theme === 'dark' ? 'light' : 'dark');
  };

  return (
    <Button
      variant="outline"
      onClick={toggleTheme}
      className="w-full justify-center gap-2 rounded-xl text-xs font-bold border-border/80 bg-background hover:bg-muted transition-all duration-300 py-5 shadow-sm"
      title="Cambiar Tema"
    >
      {theme === 'dark' ? (
        <>
          <Sun className="h-4 w-4 text-amber-500 animate-spin-slow" />
          <span>Modo Claro</span>
        </>
      ) : (
        <>
          <Moon className="h-4 w-4 text-indigo-600" />
          <span>Modo Oscuro</span>
        </>
      )}
    </Button>
  );
};

