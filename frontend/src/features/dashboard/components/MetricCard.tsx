import React from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';

interface MetricCardProps {
  icon: React.ElementType;
  iconColor: string;
  label: string;
  value: number | undefined;
  loading: boolean;
  subtitle: string;
  linkTo: string;
  linkLabel: string;
}

export const MetricCard = ({
  icon: Icon,
  iconColor,
  label,
  value,
  loading,
  subtitle,
  linkTo,
  linkLabel,
}: MetricCardProps) => {
  return (
    <Card className="relative overflow-hidden group hover:scale-[1.02] hover:shadow-lg transition-all duration-300 border border-border bg-card/40 backdrop-blur-md">
      <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity duration-300">
        <Icon className={`h-24 w-24 ${iconColor}`} />
      </div>
      <CardHeader className="pb-2">
        <CardDescription className="text-xs font-semibold tracking-wider uppercase text-muted-foreground flex items-center gap-1.5">
          <Icon className={`h-3.5 w-3.5 ${iconColor}`} /> {label}
        </CardDescription>
        <CardTitle className="text-3xl font-black mt-1">
          {loading ? <Skeleton className="h-9 w-20" /> : (value ?? 0)}
        </CardTitle>
      </CardHeader>
      <CardContent className="pb-3 text-xs text-muted-foreground font-medium">{subtitle}</CardContent>
      <CardFooter className="text-xs flex items-center border-t border-border/50 py-2.5 bg-muted/20">
        <Link
          to={linkTo}
          className={`${iconColor} hover:underline font-bold flex items-center gap-1`}
        >
          {linkLabel} <ArrowRight className="h-3 w-3" />
        </Link>
      </CardFooter>
    </Card>
  );
};

