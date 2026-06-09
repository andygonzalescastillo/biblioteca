import { Link } from 'react-router-dom';
import { BookMarked, X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Sheet, SheetContent } from '@/components/ui/sheet';
import { APP_ROUTES } from '@/shared/constants/appRoutes';
import { SidebarNav } from './SidebarNav';
import { ThemeToggle } from './ThemeToggle';

interface SidebarProps {
  sidebarOpen: boolean;
  setSidebarOpen: (open: boolean) => void;
}

const SidebarBrand = ({ onClose }: { onClose?: () => void }) => {
  return (
    <div className="flex h-16 items-center justify-between px-6 border-b border-border shrink-0">
      <Link to={APP_ROUTES.HOME} className="flex items-center gap-3 group" onClick={onClose}>
        <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-primary text-primary-foreground shadow-md shadow-primary/20 group-hover:scale-105 transition-transform duration-300">
          <BookMarked className="h-6 w-6 animate-pulse" />
        </div>
        <div>
          <span className="font-bold text-lg tracking-tight text-foreground">
            Biblioteca
          </span>
          <span className="block text-xs text-muted-foreground font-medium">
            Gestor de Sistema
          </span>
        </div>
      </Link>
      {onClose && (
        <Button variant="ghost" size="icon" className="lg:hidden" onClick={onClose}>
          <X className="h-5 w-5" />
        </Button>
      )}
    </div>
  );
};

const SidebarFooter = () => {
  return (
    <div className="p-4 border-t border-border bg-muted/30 flex items-center justify-center shrink-0">
      <ThemeToggle />
    </div>
  );
};

const SidebarPanel = ({ onClose }: { onClose?: () => void }) => {
  return (
    <div className="flex h-full flex-col">
      <SidebarBrand onClose={onClose} />
      <SidebarNav onNavigate={onClose} />
      <SidebarFooter />
    </div>
  );
};

export const Sidebar = ({ sidebarOpen, setSidebarOpen }: SidebarProps) => {
  const cerrar = () => setSidebarOpen(false);

  return (
    <>
      <aside className="hidden lg:flex lg:sticky lg:top-0 lg:h-screen lg:w-64 lg:shrink-0 lg:flex-col border-r border-border bg-card/60 backdrop-blur-md">
        <SidebarPanel />
      </aside>

      <Sheet open={sidebarOpen} onOpenChange={setSidebarOpen}>
        <SheetContent side="left" className="w-64 p-0 gap-0" showCloseButton={false}>
          <SidebarPanel onClose={cerrar} />
        </SheetContent>
      </Sheet>
    </>
  );
};

