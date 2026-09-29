import React, { Component, ForwardedRef, ReactNode, RefObject } from 'react';
import AutoNumeric from 'autonumeric';
import { InputAdornment, TextField, TextFieldProps } from '@mui/material';

// const styles = makeStyles({
//   textField: (props: CurrencyTextFieldProps) => ({
//     textAlign: props.textAlign || "right",
//   }),
// });

//interface CurrencyTextFieldProps extends WithStyles<typeof styles> {
export type CurrencyTextFieldProps = TextFieldProps & {
  type?: 'text' | 'tel' | 'hidden';
  variant?: 'standard' | 'outlined' | 'filled';
  id?: string;
  className?: string;
  style?: React.CSSProperties;
  disabled?: boolean;
  label?: string;
  textAlign?: 'right' | 'left' | 'center';
  tabIndex?: number;
  autoFocus?: boolean;
  placeholder?: string;
  value?: string | number;
  innerRef?: ForwardedRef<HTMLDivElement>;
  onChange?: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>, value: string) => void;
  onFocus?: (event: React.FocusEvent<HTMLInputElement | HTMLTextAreaElement>, value: string) => void;
  onBlur?: (event: React.FocusEvent<HTMLInputElement | HTMLTextAreaElement>, value: string) => void;
  onKeyPress?: (event: React.KeyboardEvent<HTMLInputElement | HTMLTextAreaElement>, value: string) => void;
  onKeyUp?: (event: React.KeyboardEvent<HTMLInputElement | HTMLTextAreaElement>, value: string) => void;
  onKeyDown?: (event: React.KeyboardEvent<HTMLInputElement | HTMLTextAreaElement>, value: string) => void;
  currencySymbol?: string;
  decimalCharacter?: string;
  decimalCharacterAlternative?: string;
  decimalPlaces?: number;
  decimalPlacesShownOnBlur?: number;
  decimalPlacesShownOnFocus?: number;
  digitGroupSeparator?: string;
  leadingZero?: 'allow' | 'deny' | 'keep';
  maximumValue?: string;
  minimumValue?: string;
  negativePositiveSignPlacement?: 'l' | 'r' | 'p' | 's';
  negativeSignCharacter?: string;
  outputFormat?: 'string' | 'number';
  selectOnFocus?: boolean;
  positiveSignCharacter?: string;
  readOnly?: boolean;
  preDefined?: Record<string, any>;
  inputProps?: React.InputHTMLAttributes<HTMLInputElement>;
  InputProps?: {
    startAdornment?: ReactNode;
  };
};

class CurrencyTextField extends Component<CurrencyTextFieldProps> {
  private inputRef: RefObject<HTMLInputElement> = React.createRef();
  private autonumeric: AutoNumeric | null = null;

  componentDidMount() {
    if (!this.autonumeric) {
      this.initAutonumeric(this.inputRef.current);
    }
  }

  initAutonumeric(ref: HTMLInputElement) {
    const { currencySymbol, ...others } = this.props;
    this.autonumeric = new AutoNumeric(ref, this.props.value, {
      ...this.props.preDefined,
      ...others,
      onChange: undefined,
      onFocus: undefined,
      onBlur: undefined,
      onKeyPress: undefined,
      onKeyUp: undefined,
      onKeyDown: undefined,
      watchExternalChanges: false,
    });
  }

  componentWillUnmount() {
    if (this.autonumeric) {
      this.autonumeric.remove();
    }
  }

  componentDidUpdate(prevProps: CurrencyTextFieldProps) {
    if (this.props.value !== prevProps.value) {
      if (this.autonumeric) {
        this.autonumeric.set(this.props.value);
      }
    }
  }

  getValue() {
    if (!this.autonumeric) return '';
    const valueMapper = {
      string: (numeric: AutoNumeric) => numeric.getNumericString(),
      number: (numeric: AutoNumeric) => numeric.getNumber(),
    };
    return valueMapper[this.props.outputFormat](this.autonumeric);
  }

  callEventHandler(event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>, eventName: string) {
    if (this.props[eventName]) {
      this.props[eventName](event, this.getValue());
    }

    const input = (this.inputRef.current || event.target) as HTMLInputElement;
    const currentValue = input.value;

    // console.log({
    //   fn: 'callEventHandler',
    //   eventName,
    //   currentValue,
    //   event,
    //   autonumeric: this.autonumeric
    // });

    // Focus'da Hepsini seç
    if (eventName === 'onFocus') {
      if (currentValue.trim() === '') {
        if (!this.autonumeric) {
          this.initAutonumeric(input);
        } else {
          this.autonumeric.set('0');
        }
      }
      input.setSelectionRange(0, currentValue.length);
    }
  }

  keyboardCallEventHandler(event: React.KeyboardEvent<HTMLDivElement>, eventName: string) {
    if (this.props[eventName]) {
      this.props[eventName](event, this.getValue());
    }

    const input = this.inputRef.current;
    const currentValue = input.value;
    const caretStartPosition = input.selectionStart;
    const caretEndPosition = input.selectionEnd;
    const decimalCharacterPosition = currentValue.indexOf(this.props.decimalCharacter);

    // console.log({
    //   fn: 'keyboardCallEventHandler',
    //   eventName,
    //   currentValue,
    //   eventKey: event.key,
    //   decimalCharacterPosition,
    //   caretStartPosition,
    //   caretEndPosition,
    //   autonumeric: this.autonumeric
    // });

    if (currentValue.trim() === '' || currentValue.trim() === '0') {
      this.autonumeric.set('0');
    }

    // hepsi seçili ise
    if (
      eventName === 'onKeyDown' &&
      caretEndPosition - caretStartPosition === currentValue.length &&
      '0123456789+-BackspaceDelete'.includes(event.key)
    ) {
      this.autonumeric.set('0');
      input.setSelectionRange(0, 1);
    }

    if (
      '0123456789+-'.includes(event.key) &&
      eventName === 'onKeyUp' &&
      (caretStartPosition < decimalCharacterPosition || decimalCharacterPosition === -1 || caretStartPosition !== caretEndPosition)
    ) {
      if (decimalCharacterPosition === -1) {
        input.setSelectionRange(currentValue.length, currentValue.length);
      } else {
        input.setSelectionRange(decimalCharacterPosition, decimalCharacterPosition);
      }
    }

    //Backspace
    if (event.key === 'Backspace' && eventName === 'onKeyDown') {
      if (decimalCharacterPosition === -1) {
        input.setSelectionRange(currentValue.length, currentValue.length);
      } else {
        if (caretStartPosition <= decimalCharacterPosition) {
          input.setSelectionRange(decimalCharacterPosition, decimalCharacterPosition);
        } else {
          input.setSelectionRange(caretStartPosition - 1, caretStartPosition - 1);
        }
      }
    }
    //delete
  }

  render() {
    const { classes, currencySymbol, inputProps, InputProps, innerRef, ...others } = this.props;

    const otherProps: Partial<CurrencyTextFieldProps> = {};
    [
      'id',
      'label',
      'className',
      'autoFocus',
      'variant',
      'style',
      'disabled',
      'type',
      'tabIndex',
      'placeholder',
      'size',
      'fullWidth',
    ].forEach(prop => (otherProps[prop] = this.props[prop]));

    return (
      <TextField
        sx={{
          '& .MuiInputBase-input': {
            textAlign: this.props.textAlign || 'right',
          },
        }}
        inputRef={this.inputRef}
        ref={innerRef}
        onChange={e => this.callEventHandler(e, 'onChange')}
        onFocus={e => this.callEventHandler(e, 'onFocus')}
        onBlur={e => this.callEventHandler(e, 'onBlur')}
        // onKeyPress={(e) => this.callEventHandler(e, "onKeyPress")}
        onKeyUp={e => this.keyboardCallEventHandler(e, 'onKeyUp')}
        onKeyDown={e => this.keyboardCallEventHandler(e, 'onKeyDown')}
        InputProps={{
          startAdornment: <InputAdornment position="start">{currencySymbol}</InputAdornment>,
          ...InputProps,
        }}
        inputProps={{
          // className: classes.textField,
          ...inputProps,
        }}
        {...otherProps}
      />
    );
  }
}

export default React.forwardRef<HTMLDivElement | CurrencyTextField, CurrencyTextFieldProps | any>((props, ref) => (
  <CurrencyTextField {...props} innerRef={ref} />
));

export const predefinedOptions = AutoNumeric.getPredefinedOptions();
