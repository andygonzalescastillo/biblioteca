import { useLocation } from 'react-router-dom';
import { Menu } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { menuItems } from '../menuItems';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';

interface HeaderProps {
  setSidebarOpen: (open: boolean) => void;
}

export const Header = ({ setSidebarOpen }: HeaderProps) => {
  const location = useLocation();

  const getPageTitle = () => {
    const current = menuItems.find((item) => {
      if (item.path === '/dashboard' && location.pathname === '/') return true;
      return location.pathname.startsWith(item.path);
    });
    return current ? current.label : 'Biblioteca';
  };

  return (
    <header className="flex h-16 items-center justify-between px-6 border-b border-border bg-card/40 backdrop-blur-md sticky top-0 z-30">
      <div className="flex items-center gap-4">
        <Button
          variant="ghost"
          size="icon"
          className="lg:hidden rounded-xl"
          onClick={() => setSidebarOpen(true)}
        >
          <Menu className="h-6 w-6" />
        </Button>
        <h1 className="text-xl font-bold tracking-tight">{getPageTitle()}</h1>
      </div>
      <div className="flex items-center gap-3">
        {}
        <div className="flex items-center gap-2">
          <Avatar className="h-9 w-9 border border-primary/20">
            <AvatarFallback className="bg-primary/10 text-primary font-bold text-sm">
              AG
            </AvatarFallback>
          </Avatar>
          <div className="hidden sm:block text-left">
            <span className="block text-xs font-bold leading-none">Administrador</span>
            <span className="block text-[10px] text-muted-foreground leading-tight">Andy Gonzales</span>
          </div>
        </div>
      </div>
    </header>
  );
};

