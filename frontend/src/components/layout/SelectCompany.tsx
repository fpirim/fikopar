import { useTranslation } from 'react-i18next'

export default function SelectCompany() {
  const { t } = useTranslation('shell')
  return <p className="text-sm text-fk-muted">{t('selectCompany')}</p>
}
