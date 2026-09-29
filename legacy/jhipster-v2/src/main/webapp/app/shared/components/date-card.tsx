import React from 'react';
import { useAppSelector } from 'app/config/store';
import { LONG_DATE_FORMAT, SOURCE_LONG_DATE_FORMAT } from 'app/config/constants';
import dayjs from 'dayjs';
import utc from 'dayjs/plugin/utc';
import timezone from 'dayjs/plugin/timezone';

dayjs.extend(utc);
dayjs.extend(timezone);

interface IDateCardProps {
  date?: Date;
  format?: string;
  sourceFormat?: string;
}

const DateCard = ({ date, sourceFormat = SOURCE_LONG_DATE_FORMAT, format = LONG_DATE_FORMAT }: IDateCardProps) => {
  // const account = useAppSelector(state => state.authentication.account);
  const locale = useAppSelector(state => state.locale.currentLocale);

  if (!date) return <></>;

  const formattedDate = dayjs(date, sourceFormat).locale(locale).format(format);

  return <>{`${formattedDate === 'Invalid Date' ? date : formattedDate}`}</>;
};

export default DateCard;
