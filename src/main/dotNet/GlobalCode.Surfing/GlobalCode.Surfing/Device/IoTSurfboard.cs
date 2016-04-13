using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace GlobalCode.Surfing.Device
{

    /// <summary>
    /// API de comunicação com a IoTSurfboard
    /// </summary>
    public class IoTSurfboard
    {
        private readonly IDevice _board;

        /// <summary>
        /// API de comunicação com a IoTSurfboard
        /// </summary>
        /// <param name="port">Porta de comunicação Ex: COM5</param>
        /// <param name="baudRate">Velocidade de comunicação Ex: 9600</param>
        public IoTSurfboard(string port, int baudRate)
        {
            _board = new SerialDeviceRxTx(port, baudRate);
            _board.Open();
            Thread.Sleep(2500);
        }

        /// <summary>
        /// Finaliza a comunicação com a IoTSurfboard
        /// </summary>
        public void Close()
        {
            try
            {
                _board.Close();
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message, ex);
            }
        }

        /// <summary>
        /// Retorna o valor do sensor de Alcool
        /// </summary>
        /// <returns>Valor 0-1023 do sensor</returns>
        public int Alcohol()
        {
            try
            {
                _board.Send("alcohol");
                Thread.Sleep(100);
                return int.Parse(_board.Receive());
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message, ex);
            }
        }

        /// <summary>
        /// Retorna valor do sensor de luz
        /// </summary>
        /// <returns>Valor 0-1023 do sensor</returns>
        public int Light()
        {
            try
            {
                _board.Send("light");
                Thread.Sleep(100);
                return int.Parse(_board.Receive());
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message, ex);
            }
        }

        /// <summary>
        /// Retorna valor do indicador do potenciômetro
        /// </summary>
        /// <returns>Valor 0-1023 do sensor</returns>
        public int Potentiometer()
        {
            try
            {
                _board.Send("pot");
                Thread.Sleep(100);
                return int.Parse(_board.Receive());
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message, ex);
            }
        }

        /// <summary>
        /// Retorna a temperatura
        /// </summary>
        /// <returns>Temperatura Celsius</returns>
        public float Temperature()
        {
            try
            {
                _board.Send("temp");
                Thread.Sleep(350);
                return float.Parse(_board.Receive());
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message, ex);
            }
        }

        /// <summary>
        /// Retorna o valor do sensor de umidade
        /// </summary>
        /// <returns>Porcentagem da umidade</returns>
        public float Humidity()
        {
            try
            {
                _board.Send("humidity");
                Thread.Sleep(350);
                return float.Parse(_board.Receive());
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message, ex);
            }
        }

        /// <summary>
        /// Controla o Transistor
        /// </summary>
        /// <param name="v">true = liga / false = desliga</param>
        public void Transistor(bool v)
        {
            try
            {
                _board.Send("transistor?" + (v ? "1" : "0"));
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message, ex);
            }
        }

        /// <summary>
        /// Retorna a Data/Hora do Device
        /// </summary>
        /// <returns>Data / Hora</returns>
        public string Clock()
        {
            try
            {
                _board.Send("clock");
                Thread.Sleep(100);
                return _board.Receive();
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message, ex);
            }
        }

        /// <summary>
        /// Controle o relê do device
        /// </summary>
        /// <param name="v">true = liga / false = desliga</param>
        public void Relay(bool v)
        {
            try
            {
                _board.Send("relay?" + (v ? "1" : "0"));
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message, ex);
            }
        }

        /// <summary>
        /// Controle o Speaker
        /// </summary>
        /// <param name="v">true = liga / false = desliga</param>
        public void Speaker(bool v)
        {
            try
            {
                _board.Send("speaker?" + (v ? "1" : "0"));
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message, ex);
            }

        }

        /// <summary>
        /// Aciona o Led Vermelho
        /// </summary>
        /// <param name="p">Itensidade (0=desliga)</param>
        public void Red(int p)
        {
            try
            {
                _board.Send("red?" + p);
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message, ex);
            }
        }

        /// <summary>
        /// Aciona o Led Verde
        /// </summary>
        /// <param name="p">Itensidade (0=desliga)</param>
        public void Green(int p)
        {
            try
            {
                _board.Send("green?" + p);
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message, ex);
            }
        }

        /// <summary>
        /// Aciona o Led Azul
        /// </summary>
        /// <param name="p">Itensidade (0=desliga)</param>
        public void Blue(int p)
        {
            try
            {
                _board.Send("blue?" + p);
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message, ex);
            }
        }

        /// <summary>
        /// Controla o Led RGB
        /// </summary>
        /// <param name="r">Itensidade Vermelho (0=desliga)</param>
        /// <param name="g">Itensidade Verde (0=desliga)</param>
        /// <param name="b">Itensidade Azul (0=desliga)</param>
        public void Rgb(int r, int g, int b)
        {
            try
            {
                this.Red(r);
                Thread.Sleep(50);

                this.Green(g);
                Thread.Sleep(50);

                this.Blue(b);
                Thread.Sleep(50);
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message, ex);
            }

        }


        /// <summary>
        /// Controla do Servo (Opcional)
        /// </summary>
        /// <param name="p">Posição</param>
        public void Servo(int p)
        {
            try
            {
                _board.Send("servo?" + p);
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message, ex);
            }

        }

        /// <summary>
        /// Retorna o valor do sensor de distância
        /// </summary>
        /// <returns>valor sensor de distância em CM</returns>
        public int Distance()
        {
            try
            {
                _board.Send("distance");
                Thread.Sleep(150);
                return int.Parse(_board.Receive());
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message, ex);
            }
        }

    }
}
