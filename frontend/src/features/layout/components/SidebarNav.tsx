import { NavLink, useLocation } from 'react-router-dom';
import { menuItems } from '../menuItems';
import { APP_ROUTES } from '@/shared/constants/appRoutes';

interface SidebarNavProps {
  onNavigate?: () => void;
}

export const SidebarNav = ({ onNavigate }: SidebarNavProps) => {
  const location = useLocation();

  return (
    <nav className="flex-1 space-y-1.5 px-4 py-6">
      {menuItems.map((item) => {
        const Icon = item.icon;
        const isActive =
          (item.path === APP_ROUTES.DASHBOARD
            ? location.pathname === APP_ROUTES.DASHBOARD || location.pathname === APP_ROUTES.HOME
            : location.pathname === item.path || location.pathname.startsWith(item.path + '/'));

        return (
          <NavLink
            key={item.path}
            to={item.path}
            onClick={onNavigate}
            className={`flex items-center gap-3.5 px-4 py-3 rounded-xl text-sm font-medium transition-all duration-200 group ${
              isActive
                ? 'bg-primary text-primary-foreground shadow-sm'
                : 'text-muted-foreground hover:bg-muted hover:text-foreground'
            }`}
          >
            <Icon
              className={`h-5 w-5 shrink-0 transition-transform duration-200 group-hover:scale-110 ${
                isActive
                  ? 'text-primary-foreground'
                  : 'text-muted-foreground group-hover:text-foreground'
              }`}
            />
            <span>{item.label}</span>
          </NavLink>
        );
      })}
    </nav>
  );
};

